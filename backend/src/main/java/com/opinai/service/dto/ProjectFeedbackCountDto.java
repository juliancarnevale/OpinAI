package com.opinai.service.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProjectFeedbackCountDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private long feedbackItemsCount;
}
