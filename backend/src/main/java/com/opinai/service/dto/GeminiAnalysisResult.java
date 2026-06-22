package com.opinai.service.dto;

import com.opinai.model.SentimentType;
import com.opinai.model.SentimentDistribution;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiAnalysisResult {
    private SentimentType overallSentiment;
    private String executiveSummary;
    private List<String> keyIssues;
    private List<String> improvementOpportunities;
    private SentimentDistribution sentimentDistribution;
}
