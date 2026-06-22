package com.opinai.controller;

import com.opinai.controller.dto.DashboardOverviewResponse;
import com.opinai.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Endpoints para el dashboard overview principal")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "Obtener métricas y actividad reciente para el dashboard principal")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview(Authentication authentication) {
        DashboardOverviewResponse response = dashboardService.getDashboardOverview(authentication.getName());
        return ResponseEntity.ok(response);
    }
}
