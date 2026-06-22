package com.opinai.controller;

import com.opinai.controller.dto.DashboardOverviewResponse;
import com.opinai.controller.dto.RecentAnalysisDto;
import com.opinai.controller.dto.RecentProjectDto;
import com.opinai.model.AnalysisStatus;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.DashboardService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication principal;
    private String userEmail = "dev@opinai.com";
    private DashboardOverviewResponse mockResponse;

    @BeforeEach
    void setUp() {
        principal = new UsernamePasswordAuthenticationToken(userEmail, null);

        RecentProjectDto recentProject = RecentProjectDto.builder()
                .id(UUID.randomUUID())
                .name("Proyecto Test")
                .description("Descripción Test")
                .createdAt(LocalDateTime.now())
                .feedbackItemsCount(12L)
                .build();

        RecentAnalysisDto recentAnalysis = RecentAnalysisDto.builder()
                .id(UUID.randomUUID())
                .title("Análisis Test")
                .projectName("Proyecto Test")
                .status(AnalysisStatus.COMPLETED)
                .createdAt(LocalDateTime.now())
                .build();

        mockResponse = DashboardOverviewResponse.builder()
                .totalProjects(3L)
                .totalAnalyses(10L)
                .completedAnalyses(8L)
                .activeAnalyses(2L)
                .recentProjects(List.of(recentProject))
                .recentAnalyses(List.of(recentAnalysis))
                .build();
    }

    @Test
    void getDashboardOverview_ShouldReturnResponse() throws Exception {
        when(dashboardService.getDashboardOverview(userEmail)).thenReturn(mockResponse);

        mockMvc.perform(get("/dashboard/overview")
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProjects").value(3))
                .andExpect(jsonPath("$.totalAnalyses").value(10))
                .andExpect(jsonPath("$.completedAnalyses").value(8))
                .andExpect(jsonPath("$.activeAnalyses").value(2))
                .andExpect(jsonPath("$.recentProjects[0].name").value("Proyecto Test"))
                .andExpect(jsonPath("$.recentAnalyses[0].title").value("Análisis Test"));

        verify(dashboardService, times(1)).getDashboardOverview(userEmail);
    }
}
