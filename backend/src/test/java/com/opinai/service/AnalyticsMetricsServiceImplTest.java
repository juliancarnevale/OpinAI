package com.opinai.service;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.service.analytics.impl.AnalyticsMetricsServiceImpl;
import com.opinai.service.dto.AnalysisCountsProjection;
import com.opinai.service.dto.GlobalSentimentProjection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsMetricsServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @InjectMocks
    private AnalyticsMetricsServiceImpl analyticsMetricsService;

    private User currentUser;
    private UUID projectId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(UUID.randomUUID())
                .email("dev@opinai.com")
                .build();
        projectId = UUID.randomUUID();
        startDate = LocalDateTime.now().minusDays(30);
        endDate = LocalDateTime.now();
    }

    @Test
    void populateMetrics_Success_CalculatesNssAndDelta() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(2L);

        AnalysisCountsProjection countsMock = mock(AnalysisCountsProjection.class);
        when(countsMock.getTotalAnalyses()).thenReturn(10L);
        when(countsMock.getCompletedAnalyses()).thenReturn(8L);
        when(countsMock.getActiveAnalyses()).thenReturn(2L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(countsMock);

        // Mocks para el periodo actual
        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(60L);
        when(currentProj.getNeutral()).thenReturn(30L);
        when(currentProj.getNegative()).thenReturn(10L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), eq(startDate), eq(endDate)
        )).thenReturn(currentProj);

        // Mocks para el periodo anterior equivalente (30 dias atras)
        // La duracion es de 30 dias, por lo tanto el periodo anterior inicia hace 60 dias y termina hace 30 dias.
        GlobalSentimentProjection prevProj = mock(GlobalSentimentProjection.class);
        when(prevProj.getPositive()).thenReturn(40L);
        when(prevProj.getNeutral()).thenReturn(40L);
        when(prevProj.getNegative()).thenReturn(20L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(LocalDateTime.class), eq(startDate)
        )).thenReturn(prevProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, startDate, endDate);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(2L, response.getTotalProjects());
        assertEquals(10L, response.getTotalAnalyses());
        assertEquals(8L, response.getCompletedAnalyses());
        assertEquals(2L, response.getActiveAnalyses());
        assertEquals(100L, response.getTotalFeedbacks()); // 60 + 30 + 10
        assertEquals(60.0, response.getPositiveRate()); // 60 / 100 * 100
        assertEquals(50.0, response.getNetSentimentScore()); // ((60 - 10) / 100) * 100
        assertEquals(20.0, response.getPositiveRateDelta()); // 60.0 (actual) - 40.0 (previo: 40/100*100)

        assertNotNull(response.getGlobalSentiment());
        assertEquals(60, response.getGlobalSentiment().getPositive());
        assertEquals(30, response.getGlobalSentiment().getNeutral());
        assertEquals(10, response.getGlobalSentiment().getNegative());

        verify(projectRepository, times(1)).countByUser(currentUser);
        verify(analysisRepository, times(1)).findAnalysisCounts(currentUser.getId(), projectId);
        verify(analysisRepository, times(2)).findGlobalSentimentAggregate(any(), any(), any(), any());
    }

    @Test
    void populateMetrics_WithNullTimeframes_ShouldSetDeltaToZero() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(2L);

        AnalysisCountsProjection countsMock = mock(AnalysisCountsProjection.class);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(countsMock);

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(60L);
        when(currentProj.getNeutral()).thenReturn(30L);
        when(currentProj.getNegative()).thenReturn(10L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), any()
        )).thenReturn(currentProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When (startDate y endDate son nulos)
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, null, null);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(50.0, response.getNetSentimentScore());
        assertEquals(0.0, response.getPositiveRateDelta()); // Sin rango temporal -> delta es 0

        verify(analysisRepository, times(1)).findGlobalSentimentAggregate(any(), any(), any(), any());
    }

    @Test
    void populateMetrics_100PercentPositive_CalculatesNss100AndPositiveRate100() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(10L);
        when(currentProj.getNeutral()).thenReturn(0L);
        when(currentProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), any()
        )).thenReturn(currentProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, null, null);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(100.0, response.getNetSentimentScore());
        assertEquals(100.0, response.getPositiveRate());
    }

    @Test
    void populateMetrics_100PercentNegative_CalculatesNssMinus100AndPositiveRate0() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(0L);
        when(currentProj.getNeutral()).thenReturn(0L);
        when(currentProj.getNegative()).thenReturn(10L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), any()
        )).thenReturn(currentProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, null, null);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(-100.0, response.getNetSentimentScore());
        assertEquals(0.0, response.getPositiveRate());
    }

    @Test
    void populateMetrics_BalancedMix_CalculatesNss0AndPositiveRate40() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(40L);
        when(currentProj.getNeutral()).thenReturn(20L);
        when(currentProj.getNegative()).thenReturn(40L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), any()
        )).thenReturn(currentProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, null, null);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(0.0, response.getNetSentimentScore());
        assertEquals(40.0, response.getPositiveRate());
    }

    @Test
    void populateMetrics_NoData_CalculatesNss0AndPositiveRate0() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), any()
        )).thenReturn(null);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, null, null);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(0.0, response.getNetSentimentScore());
        assertEquals(0.0, response.getPositiveRate());
    }

    @Test
    void populateMetrics_PositiveDelta_CalculatesCorrectDelta() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(75L);
        when(currentProj.getNeutral()).thenReturn(25L);
        when(currentProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), eq(startDate), eq(endDate)
        )).thenReturn(currentProj);

        GlobalSentimentProjection prevProj = mock(GlobalSentimentProjection.class);
        when(prevProj.getPositive()).thenReturn(50L);
        when(prevProj.getNeutral()).thenReturn(50L);
        when(prevProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), eq(startDate)
        )).thenReturn(prevProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, startDate, endDate);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(25.0, response.getPositiveRateDelta());
    }

    @Test
    void populateMetrics_NegativeDelta_CalculatesCorrectDelta() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(40L);
        when(currentProj.getNeutral()).thenReturn(60L);
        when(currentProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), eq(startDate), eq(endDate)
        )).thenReturn(currentProj);

        GlobalSentimentProjection prevProj = mock(GlobalSentimentProjection.class);
        when(prevProj.getPositive()).thenReturn(65L);
        when(prevProj.getNeutral()).thenReturn(35L);
        when(prevProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), eq(startDate)
        )).thenReturn(prevProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, startDate, endDate);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(-25.0, response.getPositiveRateDelta());
    }

    @Test
    void populateMetrics_NeutralDelta_CalculatesZeroDelta() {
        // Given
        when(projectRepository.countByUser(currentUser)).thenReturn(1L);
        when(analysisRepository.findAnalysisCounts(currentUser.getId(), projectId)).thenReturn(mock(AnalysisCountsProjection.class));

        GlobalSentimentProjection currentProj = mock(GlobalSentimentProjection.class);
        when(currentProj.getPositive()).thenReturn(60L);
        when(currentProj.getNeutral()).thenReturn(40L);
        when(currentProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), eq(startDate), eq(endDate)
        )).thenReturn(currentProj);

        GlobalSentimentProjection prevProj = mock(GlobalSentimentProjection.class);
        when(prevProj.getPositive()).thenReturn(60L);
        when(prevProj.getNeutral()).thenReturn(40L);
        when(prevProj.getNegative()).thenReturn(0L);
        when(analysisRepository.findGlobalSentimentAggregate(
                eq(currentUser.getId()), eq(projectId), any(), eq(startDate)
        )).thenReturn(prevProj);

        DashboardAnalyticsResponse.DashboardAnalyticsResponseBuilder builder = DashboardAnalyticsResponse.builder();

        // When
        analyticsMetricsService.populateMetrics(builder, currentUser, projectId, startDate, endDate);
        DashboardAnalyticsResponse response = builder.build();

        // Then
        assertEquals(0.0, response.getPositiveRateDelta());
    }
}
