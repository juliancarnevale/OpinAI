package com.opinai.service;

import com.opinai.client.gemini.GeminiClient;
import com.opinai.client.gemini.dto.GeminiRequestDto;
import com.opinai.client.gemini.dto.GeminiResponseDto;
import com.opinai.exception.GeminiException;
import com.opinai.exception.GeminiRateLimitException;
import com.opinai.model.SentimentType;
import com.opinai.parser.GeminiResponseParser;
import com.opinai.service.dto.GeminiAnalysisResult;
import com.opinai.model.SentimentDistribution;
import com.opinai.service.impl.GeminiServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GeminiServiceImplTest {

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private GeminiResponseParser geminiResponseParser;

    @InjectMocks
    private GeminiServiceImpl geminiService;

    private GeminiResponseDto sampleResponseDto;
    private GeminiAnalysisResult sampleResult;

    @BeforeEach
    void setUp() {
        sampleResponseDto = GeminiResponseDto.builder()
                .candidates(List.of(
                        GeminiResponseDto.Candidate.builder()
                                .content(GeminiResponseDto.Content.builder()
                                        .parts(List.of(
                                                GeminiResponseDto.Part.builder()
                                                        .text("{\"overallSentiment\":\"POSITIVE\"}")
                                                        .build()
                                        ))
                                        .build())
                                .build()
                ))
                .build();

        sampleResult = GeminiAnalysisResult.builder()
                .overallSentiment(SentimentType.POSITIVE)
                .executiveSummary("Resumen de prueba")
                .keyIssues(List.of())
                .improvementOpportunities(List.of())
                .sentimentDistribution(new SentimentDistribution(1, 0, 0))
                .build();
    }

    @Test
    void analyzeComments_WithNullComments_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> geminiService.analyzeComments(null)
        );
        assertEquals("La lista de opiniones no puede estar vacía", exception.getMessage());
    }

    @Test
    void analyzeComments_WithEmptyComments_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> geminiService.analyzeComments(Collections.emptyList())
        );
        assertEquals("La lista de opiniones no puede estar vacía", exception.getMessage());
    }

    @Test
    void analyzeComments_WithMoreThan50Comments_ShouldThrowIllegalArgumentException() {
        List<String> largeList = new ArrayList<>();
        for (int i = 0; i < 51; i++) {
            largeList.add("Comentario " + i);
        }

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> geminiService.analyzeComments(largeList)
        );
        assertEquals("El análisis admite un máximo de 50 comentarios", exception.getMessage());
    }

    @Test
    void analyzeComments_WithValidComments_ShouldCallClientAndParserAndReturnResult() {
        List<String> comments = List.of("Excelente servicio", "Muy conforme");

        when(geminiClient.generateContent(any(GeminiRequestDto.class))).thenReturn(sampleResponseDto);
        when(geminiResponseParser.parse(anyString())).thenReturn(sampleResult);

        GeminiAnalysisResult result = geminiService.analyzeComments(comments);

        assertNotNull(result);
        assertEquals(SentimentType.POSITIVE, result.getOverallSentiment());
        assertEquals("Resumen de prueba", result.getExecutiveSummary());

        // Verificar el payload enviado al cliente
        ArgumentCaptor<GeminiRequestDto> requestCaptor = ArgumentCaptor.forClass(GeminiRequestDto.class);
        verify(geminiClient).generateContent(requestCaptor.capture());
        GeminiRequestDto capturedRequest = requestCaptor.getValue();

        assertNotNull(capturedRequest);
        assertNotNull(capturedRequest.getSystemInstruction());
        assertEquals(1, capturedRequest.getContents().size());
        
        String userText = capturedRequest.getContents().get(0).getParts().get(0).getText();
        assertTrue(userText.contains("[1] Excelente servicio"));
        assertTrue(userText.contains("[2] Muy conforme"));

        // Verificar la configuración del esquema
        GeminiRequestDto.GenerationConfig config = capturedRequest.getGenerationConfig();
        assertNotNull(config);
        assertEquals("application/json", config.getResponseMimeType());
        assertNotNull(config.getResponseSchema());
        assertEquals("OBJECT", config.getResponseSchema().getType());

        // Verificar que se llamó al parser con el JSON obtenido
        verify(geminiResponseParser).parse("{\"overallSentiment\":\"POSITIVE\"}");
    }

    @Test
    void analyzeComments_WhenClientThrowsRateLimitException_ShouldPropagate() {
        List<String> comments = List.of("Excelente servicio");
        when(geminiClient.generateContent(any(GeminiRequestDto.class)))
                .thenThrow(new GeminiRateLimitException("Se ha alcanzado la cuota límite"));

        assertThrows(
                GeminiRateLimitException.class,
                () -> geminiService.analyzeComments(comments)
        );
    }

    @Test
    void analyzeComments_WhenClientResponseIsInvalid_ShouldThrowGeminiException() {
        List<String> comments = List.of("Excelente servicio");
        GeminiResponseDto invalidResponse = GeminiResponseDto.builder()
                .candidates(Collections.emptyList()) // Sin candidatos
                .build();

        when(geminiClient.generateContent(any(GeminiRequestDto.class))).thenReturn(invalidResponse);

        GeminiException exception = assertThrows(
                GeminiException.class,
                () -> geminiService.analyzeComments(comments)
        );
        assertTrue(exception.getMessage().contains("ningún candidato"));
    }
}
