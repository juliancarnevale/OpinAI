package com.opinai.service.export.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.opinai.model.ExportFormat;
import com.opinai.service.dto.AnalysisReportDTO;
import com.opinai.service.dto.ProjectReportPayload;
import com.opinai.service.export.ExporterStrategy;
import org.springframework.stereotype.Component;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class PdfExporterStrategy implements ExporterStrategy {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public ExportFormat getFormat() {
        return ExportFormat.PDF;
    }

    @Override
    public byte[] export(ProjectReportPayload payload) {
        if (payload == null) {
            throw new IllegalArgumentException("El payload del proyecto no puede ser nulo");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        // Configurar el documento PDF con márgenes limpios
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            
            // Añadir numeración de páginas en el pie
            writer.setPageEvent(new PdfPageEventHelper() {
                private final Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 9, Font.NORMAL, Color.GRAY);

                @Override
                public void onEndPage(PdfWriter writer, Document document) {
                    PdfContentByte cb = writer.getDirectContent();
                    String pageText = "Página " + writer.getPageNumber();
                    
                    cb.beginText();
                    cb.setFontAndSize(footerFont.getBaseFont(), 9);
                    cb.setColorFill(Color.GRAY);
                    // Alinear a la derecha en la parte inferior
                    cb.showTextAligned(PdfContentByte.ALIGN_RIGHT, pageText, document.right(), document.bottom() - 20, 0);
                    cb.showTextAligned(PdfContentByte.ALIGN_LEFT, "OpinAI - Reporte Ejecutivo Inmutable", document.left(), document.bottom() - 20, 0);
                    cb.endText();
                }
            });

            document.open();

            // --- PALETA DE COLORES PREMIUM ---
            Color primaryColor = new Color(59, 130, 246);  // Azul Vibrant #3B82F6
            Color textColorDark = new Color(31, 41, 55);   // Gris Slate-800
            Color textMuted = new Color(107, 114, 128);    // Gris Gray-500
            Color cardBgColor = new Color(243, 244, 246);  // Gray-100
            Color dangerBgColor = new Color(254, 242, 242); // Red-50 (Para Key Issues)
            Color dangerBorderColor = new Color(248, 113, 113); // Red-400
            Color successBgColor = new Color(240, 253, 250); // Teal-50 (Para Oportunidades)
            Color successBorderColor = new Color(45, 212, 191); // Teal-400

            // --- FUENTES CON SOPORTE UTF-8 (Cp1252) ---
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 24, Font.BOLD, primaryColor);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 12, Font.ITALIC, textMuted);
            Font sectionHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 16, Font.BOLD, textColorDark);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 10, Font.NORMAL, textColorDark);
            Font bodyBoldFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 10, Font.BOLD, textColorDark);
            Font itemHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 12, Font.BOLD, primaryColor);
            Font badgeFont = FontFactory.getFont(FontFactory.HELVETICA, BaseFont.CP1252, true, 9, Font.BOLD, Color.WHITE);

            // --- 1. CABECERA DEL INFORME ---
            Paragraph title = new Paragraph("Reporte de Análisis Ejecutivo", titleFont);
            title.setSpacingAfter(4);
            document.add(title);

            Paragraph projectInfo = new Paragraph("Proyecto: " + payload.getProjectName(), sectionHeaderFont);
            projectInfo.setSpacingAfter(4);
            document.add(projectInfo);

            Paragraph generationTime = new Paragraph("Generado el: " + LocalDateTime.now().format(DATE_FORMATTER), subtitleFont);
            generationTime.setSpacingAfter(20);
            document.add(generationTime);

            // Línea divisoria decorativa
            Paragraph separator = new Paragraph();
            separator.add(new Chunk(new com.lowagie.text.pdf.draw.LineSeparator(1.5f, 100, primaryColor, Element.ALIGN_CENTER, -2)));
            separator.setSpacingAfter(20);
            document.add(separator);

            // --- 2. DESCRIPCIÓN DEL PROYECTO ---
            if (payload.getProjectDescription() != null && !payload.getProjectDescription().trim().isEmpty()) {
                Paragraph descTitle = new Paragraph("Descripción del Proyecto", bodyBoldFont);
                descTitle.setSpacingAfter(4);
                document.add(descTitle);
                
                Paragraph descBody = new Paragraph(payload.getProjectDescription(), bodyFont);
                descBody.setSpacingAfter(20);
                document.add(descBody);
            }

            // --- 3. TABLA DE RESUMEN DE KPIs ---
            Paragraph summaryTitle = new Paragraph("Resumen Acumulado", sectionHeaderFont);
            summaryTitle.setSpacingAfter(10);
            document.add(summaryTitle);

            PdfPTable kpiTable = new PdfPTable(3);
            kpiTable.setWidthPercentage(100);
            kpiTable.setSpacingAfter(25);
            kpiTable.setWidths(new float[]{1f, 1f, 1f});

            // Celdas de KPIs
            int totalAnalyses = payload.getAnalyses() != null ? payload.getAnalyses().size() : 0;
            int totalFeedbacks = payload.getFeedbackItems() != null ? payload.getFeedbackItems().size() : 0;
            
            kpiTable.addCell(createKpiCell("Total Análisis Ejecutados", String.valueOf(totalAnalyses), cardBgColor, bodyFont, sectionHeaderFont));
            kpiTable.addCell(createKpiCell("Total Opiniones Procesadas", String.valueOf(totalFeedbacks), cardBgColor, bodyFont, sectionHeaderFont));
            
            // Tasa de Positividad Acumulada
            long positiveCount = 0;
            long totalCategorized = 0;
            if (payload.getAnalyses() != null) {
                for (AnalysisReportDTO analysis : payload.getAnalyses()) {
                    positiveCount += analysis.getPositiveCount();
                    totalCategorized += (analysis.getPositiveCount() + analysis.getNegativeCount() + analysis.getNeutralCount());
                }
            }
            String positivityRate = "N/A";
            if (totalCategorized > 0) {
                positivityRate = String.format("%.1f%%", (positiveCount * 100.0) / totalCategorized);
            }
            kpiTable.addCell(createKpiCell("Tasa Positividad Promedio", positivityRate, cardBgColor, bodyFont, sectionHeaderFont));
            document.add(kpiTable);

            // --- 4. DETALLE DE ANÁLISIS ---
            Paragraph detailsHeader = new Paragraph("Detalle Histórico de Análisis", sectionHeaderFont);
            detailsHeader.setSpacingAfter(15);
            document.add(detailsHeader);

            if (payload.getAnalyses() == null || payload.getAnalyses().isEmpty()) {
                Paragraph empty = new Paragraph("No se registran análisis completados en este proyecto.", bodyFont);
                empty.setSpacingAfter(20);
                document.add(empty);
            } else {
                for (AnalysisReportDTO analysis : payload.getAnalyses()) {
                    // Mantener el bloque del análisis agrupado en la medida de lo posible
                    Paragraph analysisBlock = new Paragraph();
                    
                    // Título del Análisis y Sentimiento Global
                    String dateStr = analysis.getCreatedAt() != null ? analysis.getCreatedAt().format(DATE_FORMATTER) : "Fecha desconocida";
                    String sentimentStr = analysis.getOverallSentiment() != null ? analysis.getOverallSentiment() : "DESCONOCIDO";

                    PdfPTable titleTable = new PdfPTable(2);
                    titleTable.setWidthPercentage(100);
                    titleTable.setWidths(new float[]{3f, 1f});
                    
                    // Celda de título y fecha
                    PdfPCell titleCell = new PdfPCell();
                    titleCell.setBorder(Rectangle.NO_BORDER);
                    titleCell.addElement(new Paragraph(analysis.getTitle(), itemHeaderFont));
                    titleCell.addElement(new Paragraph("Ejecutado el: " + dateStr, subtitleFont));
                    titleTable.addCell(titleCell);

                    // Celda de Sentimiento Badge
                    PdfPCell sentimentCell = new PdfPCell();
                    sentimentCell.setBorder(Rectangle.NO_BORDER);
                    sentimentCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    sentimentCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    
                    Color badgeBg = getSentimentColor(sentimentStr);
                    Paragraph badge = new Paragraph("  " + sentimentStr + "  ", badgeFont);
                    badge.setAlignment(Element.ALIGN_RIGHT);
                    
                    PdfPTable innerBadgeTable = new PdfPTable(1);
                    innerBadgeTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    PdfPCell innerBadgeCell = new PdfPCell(badge);
                    innerBadgeCell.setBackgroundColor(badgeBg);
                    innerBadgeCell.setPadding(4);
                    innerBadgeCell.setBorder(Rectangle.NO_BORDER);
                    innerBadgeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    innerBadgeTable.addCell(innerBadgeCell);
                    
                    sentimentCell.addElement(innerBadgeTable);
                    titleTable.addCell(sentimentCell);
                    
                    titleTable.setSpacingAfter(8);
                    document.add(titleTable);

                    // Resumen Ejecutivo
                    if (analysis.getExecutiveSummary() != null && !analysis.getExecutiveSummary().trim().isEmpty()) {
                        Paragraph summaryPara = new Paragraph(analysis.getExecutiveSummary(), bodyFont);
                        summaryPara.setSpacingAfter(10);
                        document.add(summaryPara);
                    }

                    // Key Issues (Alert Box)
                    if (analysis.getKeyIssues() != null && !analysis.getKeyIssues().isEmpty()) {
                        PdfPTable issueTable = new PdfPTable(1);
                        issueTable.setWidthPercentage(100);
                        issueTable.setSpacingAfter(8);
                        
                        PdfPCell issueCell = new PdfPCell();
                        issueCell.setBackgroundColor(dangerBgColor);
                        issueCell.setBorderColor(dangerBorderColor);
                        issueCell.setBorderWidth(1f);
                        issueCell.setPadding(10);
                        
                        Paragraph issueTitle = new Paragraph("Problemas Clave Detectados (Key Issues):", bodyBoldFont);
                        issueTitle.setSpacingAfter(4);
                        issueCell.addElement(issueTitle);
                        
                        for (String issue : analysis.getKeyIssues()) {
                            Paragraph item = new Paragraph("• " + issue, bodyFont);
                            issueCell.addElement(item);
                        }
                        issueTable.addCell(issueCell);
                        document.add(issueTable);
                    }

                    // Improvement Opportunities (Alert Box)
                    if (analysis.getImprovementOpportunities() != null && !analysis.getImprovementOpportunities().isEmpty()) {
                        PdfPTable optTable = new PdfPTable(1);
                        optTable.setWidthPercentage(100);
                        optTable.setSpacingAfter(20);
                        
                        PdfPCell optCell = new PdfPCell();
                        optCell.setBackgroundColor(successBgColor);
                        optCell.setBorderColor(successBorderColor);
                        optCell.setBorderWidth(1f);
                        optCell.setPadding(10);
                        
                        Paragraph optTitle = new Paragraph("Oportunidades de Mejora:", bodyBoldFont);
                        optTitle.setSpacingAfter(4);
                        optCell.addElement(optTitle);
                        
                        for (String opportunity : analysis.getImprovementOpportunities()) {
                            Paragraph item = new Paragraph("• " + opportunity, bodyFont);
                            optCell.addElement(item);
                        }
                        optTable.addCell(optCell);
                        document.add(optTable);
                    } else {
                        // Agregar espacio si no hay alert box de oportunidades
                        Paragraph spacer = new Paragraph(" ");
                        spacer.setSpacingAfter(15);
                        document.add(spacer);
                    }
                }
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar el reporte PDF con soporte UTF-8", e);
        }

        return baos.toByteArray();
    }

    private PdfPCell createKpiCell(String label, String value, Color bgColor, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setBorder(Rectangle.BOX);
        cell.setBorderColor(new Color(229, 231, 235)); // Gray-200
        cell.setPadding(12);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph lblPara = new Paragraph(label, labelFont);
        lblPara.setAlignment(Element.ALIGN_CENTER);
        lblPara.setSpacingAfter(4);
        cell.addElement(lblPara);
        
        Paragraph valPara = new Paragraph(value, valueFont);
        valPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valPara);
        
        return cell;
    }

    private Color getSentimentColor(String sentiment) {
        if (sentiment == null) return Color.GRAY;
        switch (sentiment.toUpperCase()) {
            case "POSITIVE":
            case "POSITIVO":
                return new Color(16, 185, 129); // Verde Esmeralda #10B981
            case "NEGATIVE":
            case "NEGATIVO":
                return new Color(239, 68, 68);  // Rojo #EF4444
            case "NEUTRAL":
                return new Color(107, 114, 128); // Gris #6B1128
            default:
                return Color.GRAY;
        }
    }
}
