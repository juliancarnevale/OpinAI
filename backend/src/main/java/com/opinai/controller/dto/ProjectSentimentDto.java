package com.opinai.controller.dto;

import com.opinai.model.SentimentDistribution;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class ProjectSentimentDto {
    private UUID projectId;
    private String projectName;
    private long totalFeedbacks;
    private SentimentDistribution sentimentDistribution;
}
