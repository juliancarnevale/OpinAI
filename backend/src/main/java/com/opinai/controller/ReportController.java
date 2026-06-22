package com.opinai.controller;

import com.opinai.controller.dto.ReportResponse;
import com.opinai.model.ExportFormat;
import com.opinai.service.ReportService;
import com.opinai.service.dto.ReportFileStream;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para generación, historial y descarga de reportes PDF y CSV")
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/projects/{projectId}/reports")
    @Operation(summary = "Solicitar la generación de un nuevo reporte (PDF o CSV)")
    public ResponseEntity<ReportResponse> createReport(
            @PathVariable UUID projectId,
            @RequestParam ExportFormat format,
            Authentication authentication) {
        ReportResponse response = reportService.createReport(projectId, format, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/reports")
    @Operation(summary = "Listar el historial de reportes asociados a un proyecto")
    public ResponseEntity<List<ReportResponse>> getReportsForProject(
            @PathVariable UUID projectId,
            Authentication authentication) {
        List<ReportResponse> list = reportService.getReportsForProject(projectId, authentication.getName());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/reports/{reportId}/download")
    @Operation(summary = "Descargar el archivo binario del reporte de forma segura")
    public ResponseEntity<StreamingResponseBody> downloadReport(
            @PathVariable UUID reportId,
            Authentication authentication) {
        
        ReportFileStream fileStream = reportService.getReportFileStream(reportId, authentication.getName());

        StreamingResponseBody responseBody = outputStream -> {
            try (InputStream is = fileStream.getInputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        };

        String contentType = fileStream.getFormat() == ExportFormat.PDF ? "application/pdf" : "text/csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileStream.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(responseBody);
    }

    @DeleteMapping("/reports/{reportId}")
    @Operation(summary = "Eliminar un reporte y su archivo físico asociado")
    public ResponseEntity<Void> deleteReport(
            @PathVariable UUID reportId,
            Authentication authentication) {
        reportService.deleteReport(reportId, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
