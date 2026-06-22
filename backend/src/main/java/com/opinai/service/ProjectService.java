package com.opinai.service;

import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.controller.dto.ProjectResponse;
import com.opinai.controller.dto.UpdateProjectRequest;

import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request, String userEmail);
    List<ProjectResponse> getProjectsForCurrentUser(String userEmail);
    ProjectResponse getProjectById(UUID id, String userEmail);
    ProjectResponse updateProject(UUID id, UpdateProjectRequest request, String userEmail);
    void deleteProject(UUID id, String userEmail);
}
