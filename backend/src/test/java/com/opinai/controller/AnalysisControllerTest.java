package com.opinai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.controller.dto.FeedbackItemResponse;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.FeedbackSourceType;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.AnalysisService;
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
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
class AnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalysisService analysisService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication principal;
    private AnalysisResponse analysisResponse;
    private AnalysisDetailResponse analysisDetailResponse;
    private String userEmail = "dev@opinai.com";

    @BeforeEach
    void setUp() {
        principal = new UsernamePasswordAuthenticationToken(userEmail, null);

        analysisResponse = AnalysisResponse.builder()
                .id(UUID.randomUUID())
                .projectId(UUID.randomUUID())
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
                .id(analysisResponse.getId())
                .projectId(analysisResponse.getProjectId())
                .projectName("Proyecto Test")
                .title("Analisis Test")
                .status(AnalysisStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .feedbackItems(Collections.singletonList(feedbackResponse))
                .build();
    }

    @Test
    void createAnalysis_Success_ReturnsCreated() throws Exception {
        // Given
        UUID projectId = analysisResponse.getProjectId();
        CreateAnalysisRequest.FeedbackItemCreateRequest itemReq = 
                new CreateAnalysisRequest.FeedbackItemCreateRequest("Muy buena atencion");
        CreateAnalysisRequest request = CreateAnalysisRequest.builder()
                .title("Analisis Test")
                .feedbackItems(Collections.singletonList(itemReq))
                .build();

        when(analysisService.createAnalysis(eq(projectId), any(CreateAnalysisRequest.class), eq(userEmail)))
                .thenReturn(analysisResponse);

        // When & Then
        mockMvc.perform(post("/projects/{projectId}/analyses", projectId)
                        .with(csrf())
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Analisis Test"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.feedbackItemsCount").value(1));

        verify(analysisService, times(1)).createAnalysis(eq(projectId), any(CreateAnalysisRequest.class), eq(userEmail));
    }

    @Test
    void createAnalysis_ValidationError_ReturnsBadRequest() throws Exception {
        // Given
        UUID projectId = UUID.randomUUID();
        CreateAnalysisRequest request = CreateAnalysisRequest.builder()
                .title("") // Título vacío
                .feedbackItems(Collections.emptyList()) // Lista vacía
                .build();

        // When & Then
        mockMvc.perform(post("/projects/{projectId}/analyses", projectId)
                        .with(csrf())
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación en los datos de entrada"));

        verify(analysisService, never()).createAnalysis(any(), any(), any());
    }

    @Test
    void getAnalysesForProject_Success_ReturnsOk() throws Exception {
        // Given
        UUID projectId = UUID.randomUUID();
        when(analysisService.getAnalysesForProject(projectId, userEmail))
                .thenReturn(Collections.singletonList(analysisResponse));

        // When & Then
        mockMvc.perform(get("/projects/{projectId}/analyses", projectId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Analisis Test"));

        verify(analysisService, times(1)).getAnalysesForProject(projectId, userEmail);
    }

    @Test
    void getAnalysisById_Success_ReturnsOk() throws Exception {
        // Given
        UUID analysisId = analysisResponse.getId();
        when(analysisService.getAnalysisById(analysisId, userEmail))
                .thenReturn(analysisDetailResponse);

        // When & Then
        mockMvc.perform(get("/analyses/{analysisId}", analysisId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Analisis Test"))
                .andExpect(jsonPath("$.feedbackItems[0].content").value("Muy buena atencion"));

        verify(analysisService, times(1)).getAnalysisById(analysisId, userEmail);
    }

    @Test
    void getAnalysisById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID analysisId = UUID.randomUUID();
        when(analysisService.getAnalysisById(analysisId, userEmail))
                .thenThrow(new ResourceNotFoundException("Análisis no encontrado o sin acceso"));

        // When & Then
        mockMvc.perform(get("/analyses/{analysisId}", analysisId)
                        .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Análisis no encontrado o sin acceso"));

        verify(analysisService, times(1)).getAnalysisById(analysisId, userEmail);
    }

    @Test
    void deleteAnalysis_Success_ReturnsNoContent() throws Exception {
        // Given
        UUID analysisId = UUID.randomUUID();
        doNothing().when(analysisService).deleteAnalysis(analysisId, userEmail);

        // When & Then
        mockMvc.perform(delete("/analyses/{analysisId}", analysisId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isNoContent());

        verify(analysisService, times(1)).deleteAnalysis(analysisId, userEmail);
    }

    @Test
    void reprocessAnalysis_Success_ReturnsAccepted() throws Exception {
        // Given
        UUID analysisId = UUID.randomUUID();
        when(analysisService.reprocessAnalysis(analysisId, userEmail)).thenReturn(analysisResponse);

        // When & Then
        mockMvc.perform(post("/analyses/{analysisId}/reprocess", analysisId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value(analysisResponse.getId().toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(analysisService, times(1)).reprocessAnalysis(analysisId, userEmail);
    }

    @Test
    void reprocessAnalysis_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID analysisId = UUID.randomUUID();
        when(analysisService.reprocessAnalysis(analysisId, userEmail))
                .thenThrow(new ResourceNotFoundException("Análisis no encontrado o sin acceso"));

        // When & Then
        mockMvc.perform(post("/analyses/{analysisId}/reprocess", analysisId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Análisis no encontrado o sin acceso"));

        verify(analysisService, times(1)).reprocessAnalysis(analysisId, userEmail);
    }

    @Test
    void reprocessAnalysis_Conflict_ReturnsConflict() throws Exception {
        // Given
        UUID analysisId = UUID.randomUUID();
        when(analysisService.reprocessAnalysis(analysisId, userEmail))
                .thenThrow(new IllegalStateException("El análisis ya se encuentra en procesamiento"));

        // When & Then
        mockMvc.perform(post("/analyses/{analysisId}/reprocess", analysisId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El análisis ya se encuentra en procesamiento"));

        verify(analysisService, times(1)).reprocessAnalysis(analysisId, userEmail);
    }
}
