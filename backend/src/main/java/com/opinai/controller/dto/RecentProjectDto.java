package com.opinai.controller.dto;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class RecentProjectDto {
    private UUID id;
    private String name;
    private String description;
    private LocalDateTime createdAt;
    private long feedbackItemsCount;
}
