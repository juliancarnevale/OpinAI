package com.opinai.parser;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.exception.GeminiParsingException;
import com.opinai.service.dto.GeminiAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    public GeminiAnalysisResult parse(String responseText) {
        if (responseText == null) {
            throw new GeminiParsingException("La respuesta de la IA es nula", null);
        }

        String cleanJson = cleanJsonText(responseText);

        try {
            return objectMapper.readValue(cleanJson, GeminiAnalysisResult.class);
        } catch (JsonProcessingException e) {
            throw new GeminiParsingException("Fallo al deserializar el JSON del análisis de IA: " + e.getMessage(), e);
        }
    }

    private String cleanJsonText(String rawText) {
        String trimmed = rawText.trim();
        
        // Remover bloques de markdown ```json ... ```
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }
        
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }
        
        return trimmed.trim();
    }
}
