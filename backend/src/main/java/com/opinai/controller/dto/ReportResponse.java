package com.opinai.controller.dto;

import com.opinai.model.ExportFormat;
import com.opinai.model.ReportStatus;
import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class ReportResponse {
    UUID id;
    UUID projectId;
    String name;
    ExportFormat format;
    long fileSize;
    ReportStatus status;
    LocalDateTime createdAt;
}
