package com.opinai.service;

import com.opinai.controller.dto.ReportResponse;
import com.opinai.model.ExportFormat;
import com.opinai.service.dto.ReportFileStream;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    ReportResponse createReport(UUID projectId, ExportFormat format, String userEmail);
    List<ReportResponse> getReportsForProject(UUID projectId, String userEmail);
    void deleteReport(UUID reportId, String userEmail);
    ReportFileStream getReportFileStream(UUID reportId, String userEmail);
}
