package com.opinai.service.dto;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class FeedbackReportDTO {
    UUID id;
    String content;
    String sourceType;
    String externalMetadata;
    LocalDateTime createdAt;
    String analysisTitle;
}
