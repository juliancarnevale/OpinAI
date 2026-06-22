package com.opinai.client.gemini;

import com.opinai.client.gemini.dto.GeminiRequestDto;
import com.opinai.client.gemini.dto.GeminiResponseDto;
import com.opinai.exception.GeminiException;
import com.opinai.exception.GeminiRateLimitException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiClient {

    private final RestClient geminiRestClient;
    private final String apiKey;
    private final String model;

    public GeminiClient(
            RestClient geminiRestClient,
            @Value("${opinai.gemini.api-key}") String apiKey,
            @Value("${opinai.gemini.model:gemini-1.5-flash}") String model) {
        this.geminiRestClient = geminiRestClient;
        this.apiKey = apiKey;
        this.model = model;
    }

    public GeminiResponseDto generateContent(GeminiRequestDto request) {
        String jsonResponse = null;
        try {
            jsonResponse = geminiRestClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/{model}:generateContent")
                            .queryParam("key", apiKey)
                            .build(model))
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == 429, (req, resp) -> {
                        throw new GeminiRateLimitException("Se ha alcanzado la cuota límite de la API de Gemini (HTTP 429)");
                    })
                    .onStatus(status -> status.isError(), (req, resp) -> {
                        throw new GeminiException("Error en respuesta de la API de Gemini. Status HTTP: " + resp.getStatusCode());
                    })
                    .body(String.class);

            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return objectMapper.readValue(jsonResponse, GeminiResponseDto.class);
        } catch (GeminiException e) {
            throw e;
        } catch (Exception e) {
            String debugSnippet = (jsonResponse != null) 
                    ? jsonResponse.substring(0, Math.min(jsonResponse.length(), 300))
                    : "No response body received";
            throw new GeminiException("Error de red al comunicarse con el proveedor de IA: " + e.getMessage() + " (Respuesta: " + debugSnippet + ")", e);
        }
    }
}
