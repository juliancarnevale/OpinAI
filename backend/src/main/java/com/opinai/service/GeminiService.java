package com.opinai.service;

import com.opinai.service.dto.GeminiAnalysisResult;

import java.util.List;

public interface GeminiService {
    GeminiAnalysisResult analyzeComments(List<String> comments);
}
