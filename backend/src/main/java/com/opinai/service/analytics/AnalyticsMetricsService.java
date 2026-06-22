package com.opinai.service.analytics;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.model.User;
import java.time.LocalDateTime;
import java.util.UUID;

public interface AnalyticsMetricsService {
    void populateMetrics(
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder,
            User user,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
