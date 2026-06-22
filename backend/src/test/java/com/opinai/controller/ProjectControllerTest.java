package com.opinai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.controller.dto.ProjectResponse;
import com.opinai.controller.dto.UpdateProjectRequest;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.ProjectService;
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

@WebMvcTest(ProjectController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    // MockBeans para evitar fallos de inicialización del filtro de seguridad
    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication principal;
    private ProjectResponse projectResponse;
    private String userEmail = "dev@opinai.com";

    @BeforeEach
    void setUp() {
        principal = new UsernamePasswordAuthenticationToken(userEmail, null);
        projectResponse = ProjectResponse.builder()
                .id(UUID.randomUUID())
                .name("Proyecto Test")
                .description("Descripcion Test")
                .userId(UUID.randomUUID())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createProject_Success_ReturnsCreated() throws Exception {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Proyecto Test", "Descripcion Test");
        when(projectService.createProject(any(CreateProjectRequest.class), eq(userEmail)))
                .thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(post("/projects")
                        .with(csrf())
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Proyecto Test"))
                .andExpect(jsonPath("$.description").value("Descripcion Test"));

        verify(projectService, times(1)).createProject(any(CreateProjectRequest.class), eq(userEmail));
    }

    @Test
    void createProject_ValidationError_ReturnsBadRequest() throws Exception {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("", "Descripcion"); // Nombre vacío

        // When & Then
        mockMvc.perform(post("/projects")
                        .with(csrf())
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error de validación en los datos de entrada"))
                .andExpect(jsonPath("$.errors[0].field").value("name"));

        verify(projectService, never()).createProject(any(), anyString());
    }

    @Test
    void getProjectsForCurrentUser_Success_ReturnsOk() throws Exception {
        // Given
        when(projectService.getProjectsForCurrentUser(userEmail))
                .thenReturn(Collections.singletonList(projectResponse));

        // When & Then
        mockMvc.perform(get("/projects")
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Proyecto Test"));

        verify(projectService, times(1)).getProjectsForCurrentUser(userEmail);
    }

    @Test
    void getProjectById_Success_ReturnsOk() throws Exception {
        // Given
        UUID projectId = projectResponse.getId();
        when(projectService.getProjectById(projectId, userEmail)).thenReturn(projectResponse);

        // When & Then
        mockMvc.perform(get("/projects/{id}", projectId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("Proyecto Test"));

        verify(projectService, times(1)).getProjectById(projectId, userEmail);
    }

    @Test
    void getProjectById_NotFound_ReturnsNotFound() throws Exception {
        // Given
        UUID projectId = UUID.randomUUID();
        when(projectService.getProjectById(projectId, userEmail))
                .thenThrow(new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));

        // When & Then
        mockMvc.perform(get("/projects/{id}", projectId)
                        .principal(principal))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Proyecto no encontrado o sin acceso"));

        verify(projectService, times(1)).getProjectById(projectId, userEmail);
    }

    @Test
    void updateProject_Success_ReturnsOk() throws Exception {
        // Given
        UUID projectId = projectResponse.getId();
        UpdateProjectRequest request = new UpdateProjectRequest("Editado", "Nueva desc");
        
        ProjectResponse editedResponse = ProjectResponse.builder()
                .id(projectId)
                .name("Editado")
                .description("Nueva desc")
                .userId(projectResponse.getUserId())
                .build();

        when(projectService.updateProject(eq(projectId), any(UpdateProjectRequest.class), eq(userEmail)))
                .thenReturn(editedResponse);

        // When & Then
        mockMvc.perform(put("/projects/{id}", projectId)
                        .with(csrf())
                        .principal(principal)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Editado"))
                .andExpect(jsonPath("$.description").value("Nueva desc"));

        verify(projectService, times(1)).updateProject(eq(projectId), any(UpdateProjectRequest.class), eq(userEmail));
    }

    @Test
    void deleteProject_Success_ReturnsNoContent() throws Exception {
        // Given
        UUID projectId = UUID.randomUUID();
        doNothing().when(projectService).deleteProject(projectId, userEmail);

        // When & Then
        mockMvc.perform(delete("/projects/{id}", projectId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(projectId, userEmail);
    }
}
