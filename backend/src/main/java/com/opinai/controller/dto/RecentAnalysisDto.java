package com.opinai.controller.dto;

import com.opinai.model.AnalysisStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecentAnalysisDto {
    private UUID id;
    private String title;
    private String projectName;
    private AnalysisStatus status;
    private LocalDateTime createdAt;
}
