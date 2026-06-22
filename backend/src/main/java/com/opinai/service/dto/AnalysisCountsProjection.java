package com.opinai.service.dto;

public interface AnalysisCountsProjection {
    long getTotalAnalyses();
    long getCompletedAnalyses();
    long getActiveAnalyses();
}
