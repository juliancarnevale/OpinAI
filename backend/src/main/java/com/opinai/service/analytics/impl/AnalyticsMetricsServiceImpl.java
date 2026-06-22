package com.opinai.service.analytics.impl;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.model.SentimentDistribution;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.service.analytics.AnalyticsMetricsService;
import com.opinai.service.dto.AnalysisCountsProjection;
import com.opinai.service.dto.GlobalSentimentProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalyticsMetricsServiceImpl implements AnalyticsMetricsService {

    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;

    @Override
    public void populateMetrics(
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder,
            User user,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        // 1. Conteo de proyectos totales del tenant
        long totalProjects = projectRepository.countByUser(user);

        // 2. Conteos consolidados de análisis (total, completados, activos) en un solo round trip
        AnalysisCountsProjection counts = analysisRepository.findAnalysisCounts(user.getId(), projectId);
        long totalAnalyses = counts != null ? counts.getTotalAnalyses() : 0L;
        long completedAnalyses = counts != null ? counts.getCompletedAnalyses() : 0L;
        long activeAnalyses = counts != null ? counts.getActiveAnalyses() : 0L;

        // 3. Agregado global de sentimientos usando Native Query JSONB
        GlobalSentimentProjection sentimentProj = analysisRepository.findGlobalSentimentAggregate(
                user.getId(), projectId, startDate, endDate
        );

        long positive = sentimentProj != null ? sentimentProj.getPositive() : 0L;
        long negative = sentimentProj != null ? sentimentProj.getNegative() : 0L;
        long neutral = sentimentProj != null ? sentimentProj.getNeutral() : 0L;
        long totalFeedbacks = positive + neutral + negative;

        // 4. Calcular métricas derivadas (Positive Rate % y Net Sentiment Score)
        double positiveRate = totalFeedbacks > 0 ? (positive * 100.0) / totalFeedbacks : 0.0;
        double netSentimentScore = totalFeedbacks > 0 ? ((positive - negative) * 100.0) / totalFeedbacks : 0.0;

        double positiveRateDelta = 0.0;
        if (startDate != null && endDate != null) {
            java.time.Duration duration = java.time.Duration.between(startDate, endDate);
            LocalDateTime prevStartDate = startDate.minus(duration);
            LocalDateTime prevEndDate = startDate;

            GlobalSentimentProjection prevSentimentProj = analysisRepository.findGlobalSentimentAggregate(
                    user.getId(), projectId, prevStartDate, prevEndDate
            );

            long prevPositive = prevSentimentProj != null ? prevSentimentProj.getPositive() : 0L;
            long prevNegative = prevSentimentProj != null ? prevSentimentProj.getNegative() : 0L;
            long prevNeutral = prevSentimentProj != null ? prevSentimentProj.getNeutral() : 0L;
            long prevTotalFeedbacks = prevPositive + prevNeutral + prevNegative;

            double prevPositiveRate = prevTotalFeedbacks > 0 ? (prevPositive * 100.0) / prevTotalFeedbacks : 0.0;
            positiveRateDelta = positiveRate - prevPositiveRate;
        }

        SentimentDistribution globalSentiment = SentimentDistribution.builder()
                .positive((int) positive)
                .negative((int) negative)
                .neutral((int) neutral)
                .build();

        builder.totalProjects(totalProjects)
                .totalAnalyses(totalAnalyses)
                .completedAnalyses(completedAnalyses)
                .activeAnalyses(activeAnalyses)
                .totalFeedbacks(totalFeedbacks)
                .positiveRate(positiveRate)
                .netSentimentScore(netSentimentScore)
                .positiveRateDelta(positiveRateDelta)
                .globalSentiment(globalSentiment);
    }
}
