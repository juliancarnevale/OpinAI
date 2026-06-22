package com.opinai.controller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAnalysisRequest {

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 255, message = "El título no puede superar los 255 caracteres")
    private String title;

    @NotEmpty(message = "Debe proporcionar al menos un comentario")
    private List<@Valid FeedbackItemCreateRequest> feedbackItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FeedbackItemCreateRequest {
        @NotBlank(message = "El contenido del comentario no puede estar vacío")
        private String content;
    }
}
