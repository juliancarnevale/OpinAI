package com.opinai.service;

import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.controller.dto.ProjectResponse;
import com.opinai.controller.dto.UpdateProjectRequest;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.mapper.ProjectMapper;
import com.opinai.model.Project;
import com.opinai.model.Role;
import com.opinai.model.User;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.impl.ProjectServiceImpl;
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
class ProjectServiceImplTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User currentUser;
    private Project myProject;
    private ProjectResponse projectResponse;
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
                .description("Descripcion de prueba")
                .user(currentUser)
                .build();

        projectResponse = ProjectResponse.builder()
                .id(myProject.getId())
                .name("Proyecto Test")
                .description("Descripcion de prueba")
                .userId(currentUser.getId())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createProject_Success() {
        // Given
        CreateProjectRequest request = new CreateProjectRequest("Proyecto Test", "Descripcion de prueba");
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectMapper.toEntity(request)).thenReturn(myProject);
        when(projectRepository.save(myProject)).thenReturn(myProject);
        when(projectMapper.toResponse(myProject)).thenReturn(projectResponse);

        // When
        ProjectResponse result = projectService.createProject(request, userEmail);

        // Then
        assertNotNull(result);
        assertEquals(myProject.getName(), result.getName());
        verify(projectRepository, times(1)).save(myProject);
    }

    @Test
    void getProjectsForCurrentUser_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByUser(currentUser)).thenReturn(Collections.singletonList(myProject));
        when(projectMapper.toResponseList(anyList())).thenReturn(Collections.singletonList(projectResponse));

        // When
        List<ProjectResponse> result = projectService.getProjectsForCurrentUser(userEmail);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(myProject.getName(), result.get(0).getName());
        verify(projectRepository, times(1)).findByUser(currentUser);
    }

    @Test
    void getProjectById_Success() {
        // Given
        UUID projectId = myProject.getId();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.of(myProject));
        when(projectMapper.toResponse(myProject)).thenReturn(projectResponse);

        // When
        ProjectResponse result = projectService.getProjectById(projectId, userEmail);

        // Then
        assertNotNull(result);
        assertEquals(projectId, result.getId());
        verify(projectRepository, times(1)).findByIdAndUser(projectId, currentUser);
    }

    @Test
    void getProjectById_NotFound_WhenNotOwnerOrNotExists() {
        // Given
        UUID otherProjectId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(otherProjectId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
                projectService.getProjectById(otherProjectId, userEmail));
        
        verify(projectRepository, times(1)).findByIdAndUser(otherProjectId, currentUser);
        verify(projectMapper, never()).toResponse(any());
    }

    @Test
    void updateProject_Success() {
        // Given
        UUID projectId = myProject.getId();
        UpdateProjectRequest request = new UpdateProjectRequest("Proyecto Editado", "Nueva desc");
        
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.of(myProject));
        
        // Simular la modificación in-place
        doAnswer(invocation -> {
            Project p = invocation.getArgument(1);
            p.setName("Proyecto Editado");
            p.setDescription("Nueva desc");
            return null;
        }).when(projectMapper).updateEntityFromRequest(eq(request), any(Project.class));
        
        Project editedProject = Project.builder()
                .id(projectId)
                .name("Proyecto Editado")
                .description("Nueva desc")
                .user(currentUser)
                .build();
        
        ProjectResponse editedResponse = ProjectResponse.builder()
                .id(projectId)
                .name("Proyecto Editado")
                .description("Nueva desc")
                .userId(currentUser.getId())
                .build();

        when(projectRepository.save(any(Project.class))).thenReturn(editedProject);
        when(projectMapper.toResponse(editedProject)).thenReturn(editedResponse);

        // When
        ProjectResponse result = projectService.updateProject(projectId, request, userEmail);

        // Then
        assertNotNull(result);
        assertEquals("Proyecto Editado", result.getName());
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void updateProject_NotFound_WhenNotOwner() {
        // Given
        UUID projectId = UUID.randomUUID();
        UpdateProjectRequest request = new UpdateProjectRequest("Edit", "Desc");
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
                projectService.updateProject(projectId, request, userEmail));
        
        verify(projectRepository, never()).save(any());
    }

    @Test
    void deleteProject_Success() {
        // Given
        UUID projectId = myProject.getId();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.of(myProject));

        // When
        projectService.deleteProject(projectId, userEmail);

        // Then
        verify(projectRepository, times(1)).delete(myProject);
    }

    @Test
    void deleteProject_NotFound_WhenNotOwner() {
        // Given
        UUID projectId = UUID.randomUUID();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
                projectService.deleteProject(projectId, userEmail));
        
        verify(projectRepository, never()).delete(any());
    }
}
