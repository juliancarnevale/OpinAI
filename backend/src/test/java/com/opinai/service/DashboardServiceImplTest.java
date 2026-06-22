package com.opinai.service;

import com.opinai.controller.dto.DashboardOverviewResponse;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.Analysis;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.Project;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.dto.ProjectFeedbackCountDto;
import com.opinai.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

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
    void getDashboardOverview_ShouldReturnResponse() {
        // Mocks
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.countByUser(currentUser)).thenReturn(5L);
        when(analysisRepository.countByUser(currentUser)).thenReturn(12L);
        when(analysisRepository.countByUserAndStatus(currentUser, AnalysisStatus.COMPLETED)).thenReturn(8L);
        when(analysisRepository.countByUserAndStatusIn(eq(currentUser), any())).thenReturn(4L);

        ProjectFeedbackCountDto recentProject = new ProjectFeedbackCountDto(
                UUID.randomUUID(), "Proyecto A", "Desc A", LocalDateTime.now(), 10L
        );
        when(projectRepository.findRecentProjectsWithFeedbackCount(eq(currentUser), any(PageRequest.class)))
                .thenReturn(List.of(recentProject));

        Project project = Project.builder().id(UUID.randomUUID()).name("Proyecto A").build();
        Analysis recentAnalysis = Analysis.builder()
                .id(UUID.randomUUID())
                .title("Análisis A")
                .project(project)
                .status(AnalysisStatus.COMPLETED)
                .build();
        recentAnalysis.setCreatedAt(LocalDateTime.now());
        when(analysisRepository.findRecentByUser(eq(currentUser), any(PageRequest.class)))
                .thenReturn(List.of(recentAnalysis));

        // Ejecución
        DashboardOverviewResponse response = dashboardService.getDashboardOverview(userEmail);

        // Aserciones
        assertNotNull(response);
        assertEquals(5, response.getTotalProjects());
        assertEquals(12, response.getTotalAnalyses());
        assertEquals(8, response.getCompletedAnalyses());
        assertEquals(4, response.getActiveAnalyses());
        assertEquals(1, response.getRecentProjects().size());
        assertEquals("Proyecto A", response.getRecentProjects().get(0).getName());
        assertEquals(10, response.getRecentProjects().get(0).getFeedbackItemsCount());
        assertEquals(1, response.getRecentAnalyses().size());
        assertEquals("Análisis A", response.getRecentAnalyses().get(0).getTitle());
        assertEquals("Proyecto A", response.getRecentAnalyses().get(0).getProjectName());

        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(projectRepository, times(1)).countByUser(currentUser);
        verify(analysisRepository, times(1)).countByUser(currentUser);
        verify(analysisRepository, times(1)).countByUserAndStatus(currentUser, AnalysisStatus.COMPLETED);
        verify(analysisRepository, times(1)).countByUserAndStatusIn(eq(currentUser), any());
    }

    @Test
    void getDashboardOverview_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                dashboardService.getDashboardOverview(userEmail)
        );

        verify(userRepository, times(1)).findByEmail(userEmail);
        verifyNoInteractions(projectRepository, analysisRepository);
    }
}
