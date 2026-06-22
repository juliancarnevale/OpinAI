package com.opinai.controller.dto;

import com.opinai.model.SentimentDistribution;
import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DashboardAnalyticsResponse {
    private long totalProjects;
    private long totalAnalyses;
    private long completedAnalyses;
    private long activeAnalyses;
    private long totalFeedbacks;
    private double positiveRate; // Métrica derivada (%)
    private double netSentimentScore; // NSS (%)
    private double positiveRateDelta; // Delta (%)
    private SentimentDistribution globalSentiment;
    private List<SentimentTrendDto> sentimentTrend;
    private List<ProjectSentimentDto> projectSentimentComparisons;
    private List<KeyIssueDto> topIssues;
    private List<ImprovementOpportunityDto> topOpportunities;
}
