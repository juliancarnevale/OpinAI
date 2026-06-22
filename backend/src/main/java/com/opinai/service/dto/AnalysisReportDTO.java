package com.opinai.service.dto;

import lombok.Builder;
import lombok.Value;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
@Builder
public class AnalysisReportDTO {
    UUID id;
    String title;
    String overallSentiment;
    String executiveSummary;
    List<String> keyIssues;
    List<String> improvementOpportunities;
    int positiveCount;
    int negativeCount;
    int neutralCount;
    LocalDateTime createdAt;
}
