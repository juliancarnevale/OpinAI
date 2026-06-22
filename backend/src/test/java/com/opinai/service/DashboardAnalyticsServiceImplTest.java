package com.opinai.service;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.User;
import com.opinai.repository.UserRepository;
import com.opinai.service.analytics.AnalyticsIssuesService;
import com.opinai.service.analytics.AnalyticsMetricsService;
import com.opinai.service.analytics.AnalyticsProjectComparisonService;
import com.opinai.service.analytics.AnalyticsTrendService;
import com.opinai.service.impl.DashboardAnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardAnalyticsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnalyticsMetricsService analyticsMetricsService;

    @Mock
    private AnalyticsTrendService analyticsTrendService;

    @Mock
    private AnalyticsIssuesService analyticsIssuesService;

    @Mock
    private AnalyticsProjectComparisonService analyticsProjectComparisonService;

    @InjectMocks
    private DashboardAnalyticsServiceImpl dashboardAnalyticsService;

    private User currentUser;
    private String userEmail = "dev@opinai.com";

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(UUID.randomUUID())
                .email(userEmail)
                .firstName("Julian")
                .lastName("FullStack")
                .build();
    }

    @Test
    void getDashboardAnalytics_ShouldOrchestrateSubServicesCorrectly() {
        // Mocks
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));

        UUID projectId = UUID.randomUUID();
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();

        // Configurar comportamiento para populateMetrics (que inyectará completedAnalyses)
        doAnswer(invocation -> {
            DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = invocation.getArgument(0);
            builder.completedAnalyses(5L); // 5 análisis completados
            return null;
        }).when(analyticsMetricsService).populateMetrics(any(), eq(currentUser), eq(projectId), eq(start), eq(end));

        // Ejecución
        DashboardAnalyticsResponse response = dashboardAnalyticsService.getDashboardAnalytics(userEmail, projectId, start, end);

        // Aserciones
        assertNotNull(response);
        assertEquals(5, response.getCompletedAnalyses());

        // Verificar interacciones y propagación de completedAnalyses a issues service
        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(analyticsMetricsService, times(1)).populateMetrics(any(), eq(currentUser), eq(projectId), eq(start), eq(end));
        verify(analyticsTrendService, times(1)).populateTrend(any(), eq(currentUser), eq(projectId), eq(start), eq(end));
        verify(analyticsIssuesService, times(1)).populateIssuesAndOpportunities(any(), eq(currentUser), eq(projectId), eq(start), eq(end), eq(5L));
        verify(analyticsProjectComparisonService, times(1)).populateComparisons(any(), eq(currentUser), eq(projectId), eq(start), eq(end));
    }

    @Test
    void getDashboardAnalytics_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                dashboardAnalyticsService.getDashboardAnalytics(userEmail, null, null, null)
        );

        verify(userRepository, times(1)).findByEmail(userEmail);
        verifyNoInteractions(analyticsMetricsService, analyticsTrendService, analyticsIssuesService, analyticsProjectComparisonService);
    }
}
