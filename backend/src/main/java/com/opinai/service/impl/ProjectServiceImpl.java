package com.opinai.service.impl;

import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.controller.dto.ProjectResponse;
import com.opinai.controller.dto.UpdateProjectRequest;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.mapper.ProjectMapper;
import com.opinai.model.Project;
import com.opinai.model.User;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectMapper.toEntity(request);
        project.setUser(user);
        Project savedProject = projectRepository.save(project);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsForCurrentUser(String userEmail) {
        User user = getUserByEmail(userEmail);
        List<Project> projects = projectRepository.findByUser(user);
        return projectMapper.toResponseList(projects);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));
        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));
        
        projectMapper.updateEntityFromRequest(request, project);
        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));
        
        projectRepository.delete(project);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
