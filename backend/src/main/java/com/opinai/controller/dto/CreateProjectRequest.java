package com.opinai.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProjectRequest {

    @NotBlank(message = "El nombre del proyecto no puede estar vacío")
    @Size(max = 100, message = "El nombre del proyecto no puede exceder los 100 caracteres")
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String description;
}
