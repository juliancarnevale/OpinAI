package com.opinai.service.export;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import com.opinai.model.ExportFormat;
import com.opinai.service.dto.AnalysisReportDTO;
import com.opinai.service.dto.FeedbackReportDTO;
import com.opinai.service.dto.ProjectReportPayload;
import com.opinai.service.export.impl.PdfExporterStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PdfExporterStrategyTest {

    private PdfExporterStrategy pdfExporter;

    @BeforeEach
    void setUp() {
        pdfExporter = new PdfExporterStrategy();
    }

    @Test
    void getFormat_ReturnsPdf() {
        assertEquals(ExportFormat.PDF, pdfExporter.getFormat());
    }

    @Test
    void export_GeneratesValidPdfDocument() throws IOException {
        // Given
        UUID analysisId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 18, 30, 0);

        AnalysisReportDTO analysis = AnalysisReportDTO.builder()
                .id(analysisId)
                .title("Análisis Ejecutivo con áéíóú y ñ")
                .overallSentiment("POSITIVO")
                .executiveSummary("Resumen de prueba con palabras acentuadas: canción, cigüeña, mañana.")
                .keyIssues(Arrays.asList("El servicio de atención al cliente es lento", "Falta de opción de pago móvil"))
                .improvementOpportunities(Collections.singletonList("Añadir pago por Bizum y tarjeta"))
                .positiveCount(10)
                .negativeCount(2)
                .neutralCount(1)
                .createdAt(now)
                .build();

        FeedbackReportDTO feedback = FeedbackReportDTO.builder()
                .id(UUID.randomUUID())
                .content("Prueba rápida de opinión")
                .sourceType("MANUAL")
                .createdAt(now)
                .analysisTitle("Análisis Ejecutivo con áéíóú y ñ")
                .build();

        ProjectReportPayload payload = ProjectReportPayload.builder()
                .projectName("Café Express Español")
                .projectDescription("Cafetería tradicional con repostería típica de España: buñuelos y torrijas.")
                .analyses(Collections.singletonList(analysis))
                .feedbackItems(Collections.singletonList(feedback))
                .build();

        // When
        byte[] pdfBytes = pdfExporter.export(payload);

        // Then
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 100, "El PDF debe contener bytes significativos");

        // Validar cabecera PDF
        String header = new String(Arrays.copyOfRange(pdfBytes, 0, 8));
        assertTrue(header.startsWith("%PDF-"));

        // Validar el contenido textual extraído para comprobar caracteres especiales á, é, í, ó, ú, ñ
        PdfReader reader = null;
        try {
            reader = new PdfReader(pdfBytes);
            assertEquals(1, reader.getNumberOfPages(), "El reporte debe caber en 1 página con estos datos");
            
            PdfTextExtractor extractor = new PdfTextExtractor(reader);
            String text = extractor.getTextFromPage(1);
            
            // Validar que las palabras acentuadas y la ñ se conservan correctamente
            assertTrue(text.contains("canción"), "Falta la palabra canción con ó acentuada");
            assertTrue(text.contains("cigüeña"), "Falta la palabra cigüeña con diéresis y ñ");
            assertTrue(text.contains("mañana"), "Falta la palabra mañana con ñ");
            assertTrue(text.contains("Español"), "Falta la palabra Español con ñ");
            assertTrue(text.contains("Análisis"), "Falta la palabra Análisis con á acentuada");
            
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
