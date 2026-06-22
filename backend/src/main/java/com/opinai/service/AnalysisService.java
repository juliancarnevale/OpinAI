package com.opinai.service;

import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.CreateAnalysisRequest;

import java.util.List;
import java.util.UUID;

public interface AnalysisService {
    AnalysisResponse createAnalysis(UUID projectId, CreateAnalysisRequest request, String userEmail);
    List<AnalysisResponse> getAnalysesForProject(UUID projectId, String userEmail);
    AnalysisDetailResponse getAnalysisById(UUID id, String userEmail);
    void deleteAnalysis(UUID id, String userEmail);
    AnalysisResponse reprocessAnalysis(UUID id, String userEmail);
}
