package com.opinai.controller.dto;

import com.opinai.model.AnalysisStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResponse {
    private UUID id;
    private UUID projectId;
    private String title;
    private AnalysisStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int feedbackItemsCount;
}
