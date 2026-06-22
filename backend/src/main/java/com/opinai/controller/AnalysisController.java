package com.opinai.controller;

import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.service.AnalysisService;
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
@RequiredArgsConstructor
@Tag(name = "Análisis", description = "Endpoints para la gestión de análisis y comentarios")
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping("/projects/{projectId}/analyses")
    @Operation(summary = "Crear un nuevo análisis para un proyecto y asociar comentarios")
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateAnalysisRequest request,
            Authentication authentication) {
        AnalysisResponse response = analysisService.createAnalysis(projectId, request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/analyses")
    @Operation(summary = "Obtener el listado de análisis asociados a un proyecto")
    public ResponseEntity<List<AnalysisResponse>> getAnalysesForProject(
            @PathVariable UUID projectId,
            Authentication authentication) {
        List<AnalysisResponse> responseList = analysisService.getAnalysesForProject(projectId, authentication.getName());
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/analyses/{analysisId}")
    @Operation(summary = "Obtener los detalles completos de un análisis específico")
    public ResponseEntity<AnalysisDetailResponse> getAnalysisById(
            @PathVariable UUID analysisId,
            Authentication authentication) {
        AnalysisDetailResponse response = analysisService.getAnalysisById(analysisId, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/analyses/{analysisId}")
    @Operation(summary = "Eliminar un análisis por su UUID")
    public ResponseEntity<Void> deleteAnalysis(
            @PathVariable UUID analysisId,
            Authentication authentication) {
        analysisService.deleteAnalysis(analysisId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/analyses/{analysisId}/reprocess")
    @Operation(summary = "Reprocesar de forma manual un análisis que falló")
    public ResponseEntity<AnalysisResponse> reprocessAnalysis(
            @PathVariable UUID analysisId,
            Authentication authentication) {
        AnalysisResponse response = analysisService.reprocessAnalysis(analysisId, authentication.getName());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}
