package com.opinai.service;

import com.opinai.model.ExportFormat;
import com.opinai.service.dto.ProjectReportPayload;

public interface ReportGeneratorService {
    byte[] generate(ProjectReportPayload payload, ExportFormat format);
}
