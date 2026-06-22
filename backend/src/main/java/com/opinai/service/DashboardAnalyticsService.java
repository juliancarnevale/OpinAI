package com.opinai.service;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public interface DashboardAnalyticsService {
    DashboardAnalyticsResponse getDashboardAnalytics(
            String userEmail,
            UUID projectId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
