package com.opinai.service.impl;

import com.opinai.controller.dto.DashboardOverviewResponse;
import com.opinai.controller.dto.RecentAnalysisDto;
import com.opinai.controller.dto.RecentProjectDto;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.Analysis;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.User;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.DashboardService;
import com.opinai.service.dto.ProjectFeedbackCountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ProjectRepository projectRepository;
    private final AnalysisRepository analysisRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview(String userEmail) {
        User user = getUserByEmail(userEmail);

        // 1. Métricas cuantitativas
        long totalProjects = projectRepository.countByUser(user);
        long totalAnalyses = analysisRepository.countByUser(user);
        long completedAnalyses = analysisRepository.countByUserAndStatus(user, AnalysisStatus.COMPLETED);
        
        // Consolidación de PENDING y PROCESSING en una única consulta JPQL
        long activeAnalyses = analysisRepository.countByUserAndStatusIn(
                user, 
                List.of(AnalysisStatus.PENDING, AnalysisStatus.PROCESSING)
        );

        // 2. Últimos 5 proyectos (Consulta optimizada agregada con Left Join)
        List<ProjectFeedbackCountDto> recentProjectsRaw = projectRepository.findRecentProjectsWithFeedbackCount(
                user, 
                PageRequest.of(0, 5)
        );
        List<RecentProjectDto> recentProjects = recentProjectsRaw.stream()
                .map(p -> RecentProjectDto.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .description(p.getDescription())
                        .createdAt(p.getCreatedAt())
                        .feedbackItemsCount(p.getFeedbackItemsCount())
                        .build())
                .collect(Collectors.toList());

        // 3. Últimos 5 análisis (Consulta optimizada JOIN FETCH para evitar N+1 en projectName)
        List<Analysis> recentAnalysesRaw = analysisRepository.findRecentByUser(
                user, 
                PageRequest.of(0, 5)
        );
        List<RecentAnalysisDto> recentAnalyses = recentAnalysesRaw.stream()
                .map(a -> RecentAnalysisDto.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .projectName(a.getProject().getName()) // Seguro, ya cargado mediante JOIN FETCH
                        .status(a.getStatus())
                        .createdAt(a.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return DashboardOverviewResponse.builder()
                .totalProjects(totalProjects)
                .totalAnalyses(totalAnalyses)
                .completedAnalyses(completedAnalyses)
                .activeAnalyses(activeAnalyses)
                .recentProjects(recentProjects)
                .recentAnalyses(recentAnalyses)
                .build();
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
