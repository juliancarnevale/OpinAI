package com.opinai.client.gemini.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeminiRequestDto {

    private List<Content> contents;
    private Content systemInstruction;
    private GenerationConfig generationConfig;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Content {
        private List<Part> parts;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Part {
        private String text;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GenerationConfig {
        private String responseMimeType;
        private ResponseSchema responseSchema;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ResponseSchema {
        private String type;
        private List<String> required;
        private Map<String, Object> properties;
        private ResponseSchema items; // Soporte para elementos de arreglos

        @JsonProperty("enum")
        private List<String> enumValues;
    }
}
