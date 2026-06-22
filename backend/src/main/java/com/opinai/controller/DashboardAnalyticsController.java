package com.opinai.controller;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.service.DashboardAnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard Analytics", description = "Endpoints para analíticas visuales de sentimientos e issues")
public class DashboardAnalyticsController {

    private final DashboardAnalyticsService dashboardAnalyticsService;

    @GetMapping("/analytics")
    @Operation(summary = "Obtener métricas agregadas, tendencias y distribución de sentimientos")
    public ResponseEntity<DashboardAnalyticsResponse> getDashboardAnalytics(
            Authentication authentication,
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        DashboardAnalyticsResponse response = dashboardAnalyticsService.getDashboardAnalytics(
                authentication.getName(), projectId, startDate, endDate
        );
        return ResponseEntity.ok(response);
    }
}
