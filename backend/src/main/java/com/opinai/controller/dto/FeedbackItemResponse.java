package com.opinai.controller.dto;

import com.opinai.model.FeedbackSourceType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackItemResponse {
    private UUID id;
    private String content;
    private FeedbackSourceType sourceType;
    private LocalDateTime createdAt;
}
