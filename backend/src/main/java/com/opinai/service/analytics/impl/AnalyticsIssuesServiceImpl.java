package com.opinai.service.analytics.impl;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.controller.dto.ImprovementOpportunityDto;
import com.opinai.controller.dto.KeyIssueDto;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.service.analytics.AnalyticsIssuesService;
import com.opinai.service.dto.KeyIssueProjection;
import com.opinai.service.dto.OpportunityProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsIssuesServiceImpl implements AnalyticsIssuesService {

    private final AnalysisRepository analysisRepository;

    @Override
    public void populateIssuesAndOpportunities(
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder,
            User user,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            long completedAnalyses
    ) {
        // 1. Obtener problemas clave ordenados por frecuencia desde PostgreSQL
        List<KeyIssueProjection> issuesProj = analysisRepository.findTopKeyIssues(
                user.getId(), projectId, startDate, endDate
        );

        List<KeyIssueDto> topIssues = issuesProj.stream()
                .map(proj -> {
                    double percentage = completedAnalyses > 0 
                            ? (proj.getCount() * 100.0) / completedAnalyses 
                            : 0.0;
                    return KeyIssueDto.builder()
                            .issue(proj.getIssue())
                            .count(proj.getCount())
                            .percentage(percentage)
                            .build();
                })
                .collect(Collectors.toList());

        // 2. Obtener oportunidades de mejora ordenadas por frecuencia desde PostgreSQL
        List<OpportunityProjection> oppProj = analysisRepository.findTopOpportunities(
                user.getId(), projectId, startDate, endDate
        );

        List<ImprovementOpportunityDto> topOpportunities = oppProj.stream()
                .map(proj -> ImprovementOpportunityDto.builder()
                        .opportunity(proj.getOpportunity())
                        .count(proj.getCount())
                        .build())
                .collect(Collectors.toList());

        builder.topIssues(topIssues)
                .topOpportunities(topOpportunities);
    }
}
