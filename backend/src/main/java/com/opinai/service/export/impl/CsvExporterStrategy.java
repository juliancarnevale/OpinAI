package com.opinai.service.export.impl;

import com.opinai.model.ExportFormat;
import com.opinai.service.dto.FeedbackReportDTO;
import com.opinai.service.dto.ProjectReportPayload;
import com.opinai.service.export.ExporterStrategy;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

@Component
public class CsvExporterStrategy implements ExporterStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.CSV;
    }

    @Override
    public byte[] export(ProjectReportPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("El payload del proyecto no puede ser nulo");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            // Escribir el BOM (Byte Order Mark) UTF-8 para compatibilidad absoluta con Microsoft Excel
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
                // Escribir Encabezado
                writer.write("ID_Analisis,Titulo_Analisis,Fecha_Analisis,Sentimiento_Global,ID_Opinion,Contenido,Origen,Metadata_Externa,Fecha_Opinion");
                writer.newLine();

                // Escribir Opiniones
                if (payload.getFeedbackItems() != null) {
                    for (FeedbackReportDTO feedback : payload.getFeedbackItems()) {
                        String analysisId = "";
                        String analysisTitle = "";
                        String analysisDate = "";
                        String overallSentiment = "";

                        // Buscar el análisis asociado para enriquecer la fila del CSV
                        if (payload.getAnalyses() != null) {
                            analysisTitle = feedback.getAnalysisTitle() != null ? feedback.getAnalysisTitle() : "";
                            // Para encontrar el ID y otros metadatos si fuera necesario
                            var matchingAnalysis = payload.getAnalyses().stream()
                                    .filter(a -> a.getTitle().equals(feedback.getAnalysisTitle()))
                                    .findFirst();
                            if (matchingAnalysis.isPresent()) {
                                var analysis = matchingAnalysis.get();
                                analysisId = analysis.getId() != null ? analysis.getId().toString() : "";
                                analysisDate = analysis.getCreatedAt() != null ? analysis.getCreatedAt().format(DATE_FORMATTER) : "";
                                overallSentiment = analysis.getOverallSentiment() != null ? analysis.getOverallSentiment() : "";
                            }
                        }

                        String feedbackId = feedback.getId() != null ? feedback.getId().toString() : "";
                        String content = feedback.getContent() != null ? feedback.getContent() : "";
                        String sourceType = feedback.getSourceType() != null ? feedback.getSourceType() : "";
                        String metadata = feedback.getExternalMetadata() != null ? feedback.getExternalMetadata() : "";
                        String feedbackDate = feedback.getCreatedAt() != null ? feedback.getCreatedAt().format(DATE_FORMATTER) : "";

                        writer.write(escapeCsvField(analysisId) + ","
                                + escapeCsvField(analysisTitle) + ","
                                + escapeCsvField(analysisDate) + ","
                                + escapeCsvField(overallSentiment) + ","
                                + escapeCsvField(feedbackId) + ","
                                + escapeCsvField(content) + ","
                                + escapeCsvField(sourceType) + ","
                                + escapeCsvField(metadata) + ","
                                + escapeCsvField(feedbackDate));
                        writer.newLine();
                    }
                }
                writer.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte CSV con soporte UTF-8", e);
        }

        return baos.toByteArray();
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "\"\"";
        }
        // Duplicar las comillas dobles internas para cumplir con el estándar RFC 4180
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
