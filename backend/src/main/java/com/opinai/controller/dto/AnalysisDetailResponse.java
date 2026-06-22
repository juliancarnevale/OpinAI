package com.opinai.controller.dto;

import com.opinai.model.AnalysisStatus;
import com.opinai.model.SentimentType;
import com.opinai.model.SentimentDistribution;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisDetailResponse {
    private UUID id;
    private UUID projectId;
    private String projectName;
    private String title;
    private AnalysisStatus status;
    private SentimentType overallSentiment;
    private String executiveSummary;
    private List<String> keyIssues;
    private List<String> improvementOpportunities;
    private SentimentDistribution sentimentDistribution;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackItemResponse> feedbackItems;
}
