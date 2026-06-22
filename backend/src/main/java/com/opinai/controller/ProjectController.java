package com.opinai.controller;

import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.controller.dto.ProjectResponse;
import com.opinai.controller.dto.UpdateProjectRequest;
import com.opinai.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Tag(name = "Proyectos", description = "Endpoints para la gestión del CRUD de proyectos")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Crear un nuevo proyecto")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        ProjectResponse response = projectService.createProject(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Listar los proyectos del usuario autenticado actual")
    public ResponseEntity<List<ProjectResponse>> getProjectsForCurrentUser(Authentication authentication) {
        List<ProjectResponse> responseList = projectService.getProjectsForCurrentUser(authentication.getName());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener detalles de un proyecto específico por UUID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable UUID id,
            Authentication authentication) {
        ProjectResponse response = projectService.getProjectById(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar el nombre y/o descripción de un proyecto existente")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication) {
        ProjectResponse response = projectService.updateProject(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un proyecto por UUID en cascada")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            Authentication authentication) {
        projectService.deleteProject(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
