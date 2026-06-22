package com.opinai.service.dto;

import java.util.UUID;

public interface ProjectSentimentProjection {
    UUID getProjectId();
    String getProjectName();
    long getTotalFeedbacks();
    long getPositive();
    long getNegative();
    long getNeutral();
}
