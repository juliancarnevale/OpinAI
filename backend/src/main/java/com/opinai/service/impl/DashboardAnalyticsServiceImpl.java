package com.opinai.service.impl;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.User;
import com.opinai.repository.UserRepository;
import com.opinai.service.DashboardAnalyticsService;
import com.opinai.service.analytics.AnalyticsIssuesService;
import com.opinai.service.analytics.AnalyticsMetricsService;
import com.opinai.service.analytics.AnalyticsProjectComparisonService;
import com.opinai.service.analytics.AnalyticsTrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardAnalyticsServiceImpl implements DashboardAnalyticsService {

    private final UserRepository userRepository;
    private final AnalyticsMetricsService analyticsMetricsService;
    private final AnalyticsTrendService analyticsTrendService;
    private final AnalyticsIssuesService analyticsIssuesService;
    private final AnalyticsProjectComparisonService analyticsProjectComparisonService;

    @Override
    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics(
            String userEmail,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // 1. Poblar métricas globales, sentimentDistribution global y positiveRate (%)
        analyticsMetricsService.populateMetrics(builder, user, projectId, startDate, endDate);

        // Recuperar completedAnalyses previamente poblado para calcular porcentajes de key issues
        DashboardAnalyticsResponse partialResponse = builder.build();
        long completedAnalyses = partialResponse.getCompletedAnalyses();

        // 2. Poblar tendencia temporal de sentimientos (rellenando huecos temporales)
        analyticsTrendService.populateTrend(builder, user, projectId, startDate, endDate);

        // 3. Poblar issues frecuentes y oportunidades
        analyticsIssuesService.populateIssuesAndOpportunities(builder, user, projectId, startDate, endDate, completedAnalyses);

        // 4. Poblar comparación de sentimientos por proyecto
        analyticsProjectComparisonService.populateComparisons(builder, user, projectId, startDate, endDate);

        return builder.build();
    }
}
