package com.opinai.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "opinai")
@Validated
@Getter
@Setter
public class OpinaiProperties {

    @Valid
    private final Jwt jwt = new Jwt();

    @Valid
    private final Gemini gemini = new Gemini();

    @Getter
    @Setter
    public static class Jwt {
        @NotBlank(message = "La clave JWT (opinai.jwt.secret) es obligatoria")
        @Size(min = 32, message = "La clave JWT (opinai.jwt.secret) debe tener al menos 32 caracteres (256 bits)")
        private String secret;

        @NotNull(message = "El tiempo de expiración de JWT (opinai.jwt.expiration-ms) es obligatorio")
        private Long expirationMs;
    }

    @Getter
    @Setter
    public static class Gemini {
        @NotBlank(message = "La API Key de Gemini (opinai.gemini.api-key) es obligatoria y no puede estar en blanco")
        private String apiKey;

        @NotBlank(message = "La URL de Gemini es obligatoria")
        private String url;

        @NotBlank(message = "El modelo de Gemini es obligatorio")
        private String model;

        @NotNull(message = "El tiempo de espera (timeout-ms) de Gemini es obligatorio")
        private Integer timeoutMs;
    }
}
