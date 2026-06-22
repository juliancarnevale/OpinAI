package com.opinai.service;

import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.controller.dto.FeedbackItemResponse;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.mapper.AnalysisMapper;
import com.opinai.model.*;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.impl.AnalysisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisServiceImplTest {

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnalysisMapper analysisMapper;

    @Mock
    private org.springframework.context.ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AnalysisServiceImpl analysisService;

    private User currentUser;
    private Project myProject;
    private Analysis myAnalysis;
    private AnalysisResponse analysisResponse;
    private AnalysisDetailResponse analysisDetailResponse;
    private String userEmail = "dev@opinai.com";

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(UUID.randomUUID())
                .email(userEmail)
                .firstName("Julian")
                .lastName("FullStack")
                .role(Role.ROLE_USER)
                .build();

        myProject = Project.builder()
                .id(UUID.randomUUID())
                .name("Proyecto Test")
                .description("Proyecto de prueba")
                .user(currentUser)
                .build();

        myAnalysis = Analysis.builder()
                .id(UUID.randomUUID())
                .title("Analisis Test")
                .status(AnalysisStatus.PENDING)
                .project(myProject)
                .build();

        analysisResponse = AnalysisResponse.builder()
                .id(myAnalysis.getId())
                .projectId(myProject.getId())
                .title("Analisis Test")
                .status(AnalysisStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedbackItemsCount(1)
                .build();

        FeedbackItemResponse feedbackResponse = FeedbackItemResponse.builder()
                .id(UUID.randomUUID())
                .content("Muy buena atencion")
                .sourceType(FeedbackSourceType.MANUAL)
                .createdAt(LocalDateTime.now())
                .build();

        analysisDetailResponse = AnalysisDetailResponse.builder()
                .id(myAnalysis.getId())
                .projectId(myProject.getId())
                .projectName(myProject.getName())
                .title("Analisis Test")
                .status(AnalysisStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedbackItems(Collections.singletonList(feedbackResponse))
                .build();
    }

    @Test
    void createAnalysis_Success() {
        // Given
        CreateAnalysisRequest.FeedbackItemCreateRequest itemReq = 
                new CreateAnalysisRequest.FeedbackItemCreateRequest("Muy buena atencion");
        CreateAnalysisRequest request = CreateAnalysisRequest.builder()
                .title("Analisis Test")
                .feedbackItems(Collections.singletonList(itemReq))
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(myProject.getId(), currentUser)).thenReturn(Optional.of(myProject));
        when(analysisRepository.save(any(Analysis.class))).thenReturn(myAnalysis);
        when(analysisMapper.toResponse(any(Analysis.class))).thenReturn(analysisResponse);

        // When
        AnalysisResponse result = analysisService.createAnalysis(myProject.getId(), request, userEmail);

        // Then
        assertNotNull(result);
        assertEquals("Analisis Test", result.getTitle());
        verify(analysisRepository, times(1)).save(any(Analysis.class));
    }

    @Test
    void createAnalysis_ProjectNotFound_WhenNotOwner() {
        // Given
        UUID otherProjectId = UUID.randomUUID();
        CreateAnalysisRequest request = CreateAnalysisRequest.builder()
                .title("Analisis Test")
                .feedbackItems(Collections.emptyList())
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(otherProjectId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.createAnalysis(otherProjectId, request, userEmail));

        verify(analysisRepository, never()).save(any());
    }

    @Test
    void getAnalysesForProject_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(myProject.getId(), currentUser)).thenReturn(Optional.of(myProject));
        when(analysisRepository.findByProjectOrderByCreatedAtDesc(myProject)).thenReturn(Collections.singletonList(myAnalysis));
        when(analysisMapper.toResponseList(anyList())).thenReturn(Collections.singletonList(analysisResponse));

        // When
        List<AnalysisResponse> result = analysisService.getAnalysesForProject(myProject.getId(), userEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Analisis Test", result.get(0).getTitle());
        verify(analysisRepository, times(1)).findByProjectOrderByCreatedAtDesc(myProject);
    }

    @Test
    void getAnalysesForProject_NotFound_WhenNotOwner() {
        // Given
        UUID otherProjectId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(otherProjectId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.getAnalysesForProject(otherProjectId, userEmail));

        verify(analysisRepository, never()).findByProjectOrderByCreatedAtDesc(any());
    }

    @Test
    void getAnalysisById_Success() {
        // Given
        UUID analysisId = myAnalysis.getId();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.findByIdAndUser(analysisId, currentUser)).thenReturn(Optional.of(myAnalysis));
        when(analysisMapper.toDetailResponse(myAnalysis)).thenReturn(analysisDetailResponse);

        // When
        AnalysisDetailResponse result = analysisService.getAnalysisById(analysisId, userEmail);

        // Then
        assertNotNull(result);
        assertEquals(analysisId, result.getId());
        assertEquals("Analisis Test", result.getTitle());
        verify(analysisRepository, times(1)).findByIdAndUser(analysisId, currentUser);
    }

    @Test
    void getAnalysisById_NotFound_WhenNotOwner() {
        // Given
        UUID otherAnalysisId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.findByIdAndUser(otherAnalysisId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.getAnalysisById(otherAnalysisId, userEmail));

        verify(analysisRepository, times(1)).findByIdAndUser(otherAnalysisId, currentUser);
        verify(analysisMapper, never()).toDetailResponse(any());
    }

    @Test
    void deleteAnalysis_Success() {
        // Given
        UUID analysisId = myAnalysis.getId();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.findByIdAndUser(analysisId, currentUser)).thenReturn(Optional.of(myAnalysis));

        // When
        analysisService.deleteAnalysis(analysisId, userEmail);

        // Then
        verify(analysisRepository, times(1)).delete(myAnalysis);
    }

    @Test
    void deleteAnalysis_NotFound_WhenNotOwner() {
        // Given
        UUID otherAnalysisId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.findByIdAndUser(otherAnalysisId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.deleteAnalysis(otherAnalysisId, userEmail));

        verify(analysisRepository, never()).delete(any());
    }

    @Test
    void reprocessAnalysis_Success_WhenFailed() {
        // Given
        UUID analysisId = myAnalysis.getId();
        myAnalysis.setStatus(AnalysisStatus.FAILED);
        myAnalysis.setOverallSentiment(SentimentType.POSITIVE);
        myAnalysis.setExecutiveSummary("Viejo resumen");

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.reprocessAtomic(analysisId, currentUser)).thenReturn(1);
        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(myAnalysis));
        when(analysisMapper.toResponse(any(Analysis.class))).thenReturn(
                AnalysisResponse.builder()
                        .id(analysisId)
                        .status(AnalysisStatus.PENDING)
                        .build()
        );

        // When
        AnalysisResponse response = analysisService.reprocessAnalysis(analysisId, userEmail);

        // Then
        assertNotNull(response);
        verify(eventPublisher, times(1)).publishEvent(any(com.opinai.event.AnalysisProcessingRequestedEvent.class));
        verify(analysisRepository, never()).save(any());
    }

    @Test
    void reprocessAnalysis_ThrowsIllegalStateException_WhenProcessing() {
        // Given
        UUID analysisId = myAnalysis.getId();
        myAnalysis.setStatus(AnalysisStatus.PROCESSING);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.reprocessAtomic(analysisId, currentUser)).thenReturn(0);
        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(myAnalysis));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                analysisService.reprocessAnalysis(analysisId, userEmail));
        assertTrue(exception.getMessage().contains("procesamiento"));

        verify(eventPublisher, never()).publishEvent(any());
        verify(analysisRepository, never()).save(any());
    }

    @Test
    void reprocessAnalysis_ThrowsIllegalStateException_WhenCompleted() {
        // Given
        UUID analysisId = myAnalysis.getId();
        myAnalysis.setStatus(AnalysisStatus.COMPLETED);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.reprocessAtomic(analysisId, currentUser)).thenReturn(0);
        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(myAnalysis));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                analysisService.reprocessAnalysis(analysisId, userEmail));
        assertTrue(exception.getMessage().contains("completado"));

        verify(eventPublisher, never()).publishEvent(any());
        verify(analysisRepository, never()).save(any());
    }

    @Test
    void reprocessAnalysis_NotFound_WhenNotOwner() {
        // Given
        UUID otherAnalysisId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(analysisRepository.reprocessAtomic(otherAnalysisId, currentUser)).thenReturn(0);
        when(analysisRepository.findById(otherAnalysisId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () ->
                analysisService.reprocessAnalysis(otherAnalysisId, userEmail));

        verify(eventPublisher, never()).publishEvent(any());
        verify(analysisRepository, never()).save(any());
    }
}
