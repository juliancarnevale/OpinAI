package com.opinai.controller;

import com.opinai.controller.dto.DashboardAnalyticsResponse;
import com.opinai.controller.dto.KeyIssueDto;
import com.opinai.controller.dto.RecentProjectDto;
import com.opinai.model.SentimentDistribution;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.DashboardAnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardAnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardAnalyticsService dashboardAnalyticsService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication principal;
    private String userEmail = "dev@opinai.com";
    private DashboardAnalyticsResponse mockResponse;

    @BeforeEach
    void setUp() {
        principal = new UsernamePasswordAuthenticationToken(userEmail, null);

        SentimentDistribution dist = SentimentDistribution.builder()
                .positive(10)
                .negative(5)
                .neutral(2)
                .build();

        mockResponse = DashboardAnalyticsResponse.builder()
                .totalProjects(3L)
                .totalAnalyses(15L)
                .completedAnalyses(10L)
                .activeAnalyses(5L)
                .totalFeedbacks(17L)
                .positiveRate(58.82)
                .globalSentiment(dist)
                .topIssues(List.of(KeyIssueDto.builder().issue("Problema A").count(4L).percentage(40.0).build()))
                .build();
    }

    @Test
    void getDashboardAnalytics_ShouldReturnResponse() throws Exception {
        UUID projectId = UUID.randomUUID();

        when(dashboardAnalyticsService.getDashboardAnalytics(eq(userEmail), eq(projectId), any(), any()))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/dashboard/analytics")
                        .principal(principal)
                        .param("projectId", projectId.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(3))
                .andExpect(jsonPath("$.totalAnalyses").value(15))
                .andExpect(jsonPath("$.completedAnalyses").value(10))
                .andExpect(jsonPath("$.totalFeedbacks").value(17))
                .andExpect(jsonPath("$.positiveRate").value(58.82))
                .andExpect(jsonPath("$.globalSentiment.positive").value(10))
                .andExpect(jsonPath("$.topIssues[0].issue").value("Problema A"))
                .andExpect(jsonPath("$.topIssues[0].percentage").value(40.0));

        verify(dashboardAnalyticsService, times(1))
                .getDashboardAnalytics(eq(userEmail), eq(projectId), any(), any());
    }
}
