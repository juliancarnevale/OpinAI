package com.opinai.controller.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class DashboardOverviewResponse {
    private long totalProjects;
    private long totalAnalyses;
    private long completedAnalyses;
    private long activeAnalyses;
    private List<RecentProjectDto> recentProjects;
    private List<RecentAnalysisDto> recentAnalyses;
}
