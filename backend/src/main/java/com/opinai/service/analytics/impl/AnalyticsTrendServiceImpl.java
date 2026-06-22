package com.opinai.service.analytics.impl;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.controller.dto.SentimentTrendDto;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.service.analytics.AnalyticsTrendService;
import com.opinai.service.dto.SentimentTrendProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsTrendServiceImpl implements AnalyticsTrendService {

    private final AnalysisRepository analysisRepository;

    @Override
    public void populateTrend(
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder,
            User user,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {
        List<SentimentTrendProjection> projections = analysisRepository.findSentimentTrend(
                user.getId(), projectId, startDate, endDate
        );

        // Mapear proyecciones por fecha para acceso rápido
        Map<LocalDate, SentimentTrendProjection> projectionMap = projections.stream()
                .collect(Collectors.toMap(
                        SentimentTrendProjection::getTrendDate,
                        p -> p,
                        (p1, p2) -> p1 // En caso de duplicados, mantener el primero
                ));

        // Determinar límites de fechas para rellenar vacíos
        LocalDate start;
        LocalDate end = (endDate != null) ? endDate.toLocalDate() : LocalDate.now();

        if (startDate != null) {
            start = startDate.toLocalDate();
        } else if (!projections.isEmpty()) {
            start = projections.get(0).getTrendDate();
        } else {
            start = LocalDate.now().minusDays(30); // Por defecto 30 días si no hay datos ni filtro
        }

        List<SentimentTrendDto> trendList = new ArrayList<>();
        LocalDate current = start;

        // Bucle temporal para rellenar huecos con valores en cero
        while (!current.isAfter(end)) {
            SentimentTrendProjection proj = projectionMap.get(current);
            if (proj != null) {
                trendList.add(SentimentTrendDto.builder()
                        .date(current)
                        .positive(proj.getPositive())
                        .negative(proj.getNegative())
                        .neutral(proj.getNeutral())
                        .analysisCount(proj.getAnalysisCount())
                        .build());
            } else {
                trendList.add(SentimentTrendDto.builder()
                        .date(current)
                        .positive(0L)
                        .negative(0L)
                        .neutral(0L)
                        .analysisCount(0L)
                        .build());
            }
            current = current.plusDays(1);
        }

        builder.sentimentTrend(trendList);
    }
}
