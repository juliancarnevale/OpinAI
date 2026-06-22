package com.opinai.service.mapper;

import com.opinai.model.*;
import com.opinai.service.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReportPayloadAssembler {

    public ProjectReportPayload assemble(Project project, List<Analysis> analyses) {
        if (project == null) {
            throw new IllegalArgumentException("El proyecto no puede ser nulo");
        }
        if (analyses == null) {
            throw new IllegalArgumentException("La lista de análisis no puede ser nula");
        }

        List<AnalysisReportDTO> analysisDTOs = analyses.stream()
                .map(a -> AnalysisReportDTO.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .overallSentiment(a.getOverallSentiment() != null ? a.getOverallSentiment().name() : null)
                        .executiveSummary(a.getExecutiveSummary())
                        .keyIssues(a.getKeyIssues())
                        .improvementOpportunities(a.getImprovementOpportunities())
                        .positiveCount(a.getSentimentDistribution() != null ? a.getSentimentDistribution().getPositive() : 0)
                        .negativeCount(a.getSentimentDistribution() != null ? a.getSentimentDistribution().getNegative() : 0)
                        .neutralCount(a.getSentimentDistribution() != null ? a.getSentimentDistribution().getNeutral() : 0)
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        List<FeedbackReportDTO> feedbackDTOs = analyses.stream()
                .flatMap(a -> a.getFeedbackItems().stream().map(f -> FeedbackReportDTO.builder()
                        .id(f.getId())
                        .content(f.getContent())
                        .sourceType(f.getSourceType() != null ? f.getSourceType().name() : null)
                        .externalMetadata(f.getExternalMetadata())
                        .createdAt(f.getCreatedAt())
                        .analysisTitle(a.getTitle())
                        .build()))
                .collect(Collectors.toList());

        return ProjectReportPayload.builder()
                .projectName(project.getName())
                .projectDescription(project.getDescription())
                .analyses(analysisDTOs)
                .feedbackItems(feedbackDTOs)
                .build();
    }
}
