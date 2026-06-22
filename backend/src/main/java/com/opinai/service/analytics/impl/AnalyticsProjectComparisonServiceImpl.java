package com.opinai.service.analytics.impl;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.controller.dto.ProjectSentimentDto;
import com.opinai.model.SentimentDistribution;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.service.analytics.AnalyticsProjectComparisonService;
import com.opinai.service.dto.ProjectSentimentProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsProjectComparisonServiceImpl implements AnalyticsProjectComparisonService {

    private final AnalysisRepository analysisRepository;

    @Override
    public void populateComparisons(
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder,
            User user,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<ProjectSentimentProjection> comparisonsProj = analysisRepository.findProjectSentimentComparisons(
                user.getId(), projectId, startDate, endDate
        );

        List<ProjectSentimentDto> projectSentimentComparisons = comparisonsProj.stream()
                .map(proj -> {
                    SentimentDistribution dist = SentimentDistribution.builder()
                            .positive((int) proj.getPositive())
                            .negative((int) proj.getNegative())
                            .neutral((int) proj.getNeutral())
                            .build();

                    return ProjectSentimentDto.builder()
                            .projectId(proj.getProjectId())
                            .projectName(proj.getProjectName())
                            .totalFeedbacks(proj.getTotalFeedbacks())
                            .sentimentDistribution(dist)
                            .build();
                })
                .collect(Collectors.toList());

        builder.projectSentimentComparisons(projectSentimentComparisons);
    }
}
