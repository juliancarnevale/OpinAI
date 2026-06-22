package com.opinai.service.export;

import com.opinai.model.ExportFormat;
import com.opinai.service.dto.ProjectReportPayload;

public interface ExporterStrategy {
    ExportFormat getFormat();
    byte[] export(ProjectReportPayload payload);
}
