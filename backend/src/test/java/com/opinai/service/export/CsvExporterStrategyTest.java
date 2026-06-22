package com.opinai.service.export;

import com.opinai.model.ExportFormat;
import com.opinai.service.dto.AnalysisReportDTO;
import com.opinai.service.dto.FeedbackReportDTO;
import com.opinai.service.dto.ProjectReportPayload;
import com.opinai.service.export.impl.CsvExporterStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CsvExporterStrategyTest {

    private CsvExporterStrategy csvExporter;

    @BeforeEach
    void setUp() {
        csvExporter = new CsvExporterStrategy();
    }

    @Test
    void getFormat_ReturnsCsv() {
        assertEquals(ExportFormat.CSV, csvExporter.getFormat());
    }

    @Test
    void export_GeneratesValidCsvWithUtf8BomAndAccents() throws IOException {
        // Given
        UUID analysisId = UUID.randomUUID();
        UUID feedbackId1 = UUID.randomUUID();
        UUID feedbackId2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2026, 6, 15, 18, 30, 0);

        AnalysisReportDTO analysis = AnalysisReportDTO.builder()
                .id(analysisId)
                .title("Análisis de Prueba")
                .overallSentiment("POSITIVO")
                .createdAt(now)
                .build();

        FeedbackReportDTO feedback1 = FeedbackReportDTO.builder()
                .id(feedbackId1)
                .content("Excelente atención, los camareros son muy amables y rápidos.")
                .sourceType("MANUAL")
                .createdAt(now)
                .analysisTitle("Análisis de Prueba")
                .build();

        FeedbackReportDTO feedback2 = FeedbackReportDTO.builder()
                .id(feedbackId2)
                .content("La comida está fría y la mesa tenía polvo, fatal.")
                .sourceType("API")
                .createdAt(now)
                .analysisTitle("Análisis de Prueba")
                .build();

        ProjectReportPayload payload = ProjectReportPayload.builder()
                .projectName("Café Central")
                .projectDescription("Cafetería en el centro histórico")
                .analyses(Collections.singletonList(analysis))
                .feedbackItems(Arrays.asList(feedback1, feedback2))
                .build();

        // When
        byte[] csvBytes = csvExporter.export(payload);

        // Then
        assertNotNull(csvBytes);
        assertTrue(csvBytes.length > 3, "El archivo debe tener al menos el BOM");

        // Validar el BOM UTF-8 (EF BB BF)
        assertEquals((byte) 0xEF, csvBytes[0]);
        assertEquals((byte) 0xBB, csvBytes[1]);
        assertEquals((byte) 0xBF, csvBytes[2]);

        // Leer el contenido del CSV omitiendo el BOM
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8);

        // Validar cabecera
        assertTrue(csvContent.startsWith("ID_Analisis,Titulo_Analisis,Fecha_Analisis,Sentimiento_Global,ID_Opinion,Contenido,Origen,Metadata_Externa,Fecha_Opinion"));

        // Validar contenido y acentos/ñ
        assertTrue(csvContent.contains("Análisis de Prueba"));
        assertTrue(csvContent.contains("Excelente atención, los camareros son muy amables y rápidos."));
        assertTrue(csvContent.contains("La comida está fría y la mesa tenía polvo, fatal."));
        assertTrue(csvContent.contains("POSITIVO"));
        assertTrue(csvContent.contains("MANUAL"));

        // Validar caracteres específicos á, é, í, ó, ú, ñ
        assertTrue(csvContent.contains("atención")); // ó
        assertTrue(csvContent.contains("fría"));     // í
        assertTrue(csvContent.contains("tenía"));    // í
    }

    @Test
    void export_EscapesSpecialCharactersCorrectly() {
        // Given
        UUID analysisId = UUID.randomUUID();
        UUID feedbackId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        AnalysisReportDTO analysis = AnalysisReportDTO.builder()
                .id(analysisId)
                .title("Análisis con \"Comillas\" y , Comas")
                .overallSentiment("NEUTRAL")
                .createdAt(now)
                .build();

        FeedbackReportDTO feedback = FeedbackReportDTO.builder()
                .id(feedbackId)
                .content("Opinión \"urgente\", necesita revisión rápido.")
                .sourceType("API")
                .createdAt(now)
                .analysisTitle("Análisis con \"Comillas\" y , Comas")
                .build();

        ProjectReportPayload payload = ProjectReportPayload.builder()
                .projectName("Test")
                .analyses(Collections.singletonList(analysis))
                .feedbackItems(Collections.singletonList(feedback))
                .build();

        // When
        byte[] csvBytes = csvExporter.export(payload);
        String csvContent = new String(csvBytes, 3, csvBytes.length - 3, StandardCharsets.UTF_8);

        // Then
        // Las comillas internas deben escaparse duplicándolas y envolver la columna entre comillas
        assertTrue(csvContent.contains("\"Análisis con \"\"Comillas\"\" y , Comas\""));
        assertTrue(csvContent.contains("\"Opinión \"\"urgente\"\", necesita revisión rápido.\""));
    }
}
