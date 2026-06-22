package com.opinai.controller.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
