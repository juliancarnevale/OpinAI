package com.opinai.service.impl;

import com.opinai.client.gemini.GeminiClient;
import com.opinai.client.gemini.dto.GeminiRequestDto;
import com.opinai.client.gemini.dto.GeminiResponseDto;
import com.opinai.exception.GeminiException;
import com.opinai.parser.GeminiResponseParser;
import com.opinai.service.GeminiService;
import com.opinai.service.dto.GeminiAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiServiceImpl implements GeminiService {

    private final GeminiClient geminiClient;
    private final GeminiResponseParser geminiResponseParser;

    @Override
    public GeminiAnalysisResult analyzeComments(List<String> comments) {
        if (comments == null || comments.isEmpty()) {
            throw new IllegalArgumentException("La lista de opiniones no puede estar vacía");
        }
        if (comments.size() > 50) {
            throw new IllegalArgumentException("El análisis admite un máximo de 50 comentarios");
        }

        StringBuilder userPrompt = new StringBuilder("Analiza las siguientes opiniones de clientes:\n");
        for (int i = 0; i < comments.size(); i++) {
            String comment = comments.get(i);
            userPrompt.append(String.format("[%d] %s\n", i + 1, comment != null ? comment.trim() : ""));
        }

        String systemInstructionText = "Eres un experto analista de opiniones de clientes. Tu tarea es analizar una lista de comentarios recibidos y extraer los siguientes datos en formato JSON de acuerdo con el esquema proporcionado:\n" +
                "1. Sentimiento general (overallSentiment): Debe ser POSITIVE, NEGATIVE o NEUTRAL. Si no puedes determinar el sentimiento de una opinión con suficiente confianza o resulta ambigua, clasifícala por defecto como NEUTRAL.\n" +
                "2. Resumen ejecutivo (executiveSummary): Un resumen claro, conciso y profesional del conjunto de opiniones (en español).\n" +
                "3. Problemas clave (keyIssues): Una lista estructurada con los problemas recurrentes mencionados en los comentarios (en español).\n" +
                "4. Oportunidades de mejora (improvementOpportunities): Una lista estructurada con recomendaciones de mejora basadas en el análisis (en español).\n" +
                "5. Distribución de sentimientos (sentimentDistribution): Un conteo exacto de cuántas opiniones se clasifican como positivas (positive), negativas (negative) y neutras (neutral). La suma de la distribución debe ser exactamente igual al número total de opiniones analizadas.";

        GeminiRequestDto.Content systemInstruction = GeminiRequestDto.Content.builder()
                .parts(List.of(GeminiRequestDto.Part.builder()
                        .text(systemInstructionText)
                        .build()))
                .build();

        GeminiRequestDto.ResponseSchema responseSchema = buildResponseSchema();

        GeminiRequestDto.GenerationConfig generationConfig = GeminiRequestDto.GenerationConfig.builder()
                .responseMimeType("application/json")
                .responseSchema(responseSchema)
                .build();

        GeminiRequestDto.Content userContent = GeminiRequestDto.Content.builder()
                .parts(List.of(GeminiRequestDto.Part.builder()
                        .text(userPrompt.toString())
                        .build()))
                .build();

        GeminiRequestDto request = GeminiRequestDto.builder()
                .contents(List.of(userContent))
                .systemInstruction(systemInstruction)
                .generationConfig(generationConfig)
                .build();

        GeminiResponseDto response = geminiClient.generateContent(request);

        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new GeminiException("La API de Gemini no devolvió ningún candidato de respuesta");
        }

        GeminiResponseDto.Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null || candidate.getContent().getParts().isEmpty()) {
            throw new GeminiException("El candidato de respuesta no contiene partes de contenido");
        }

        String rawJson = candidate.getContent().getParts().get(0).getText();

        return geminiResponseParser.parse(rawJson);
    }

    private GeminiRequestDto.ResponseSchema buildResponseSchema() {
        // Esquemas individuales para la distribución de sentimientos
        GeminiRequestDto.ResponseSchema positiveCountSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("INTEGER")
                .build();
        GeminiRequestDto.ResponseSchema negativeCountSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("INTEGER")
                .build();
        GeminiRequestDto.ResponseSchema neutralCountSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("INTEGER")
                .build();

        GeminiRequestDto.ResponseSchema sentimentDistributionSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("OBJECT")
                .properties(Map.of(
                        "positive", positiveCountSchema,
                        "negative", negativeCountSchema,
                        "neutral", neutralCountSchema
                ))
                .required(List.of("positive", "negative", "neutral"))
                .build();

        // Esquema para la lista de problemas clave
        GeminiRequestDto.ResponseSchema keyIssueItemSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("STRING")
                .build();
        GeminiRequestDto.ResponseSchema keyIssuesSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("ARRAY")
                .items(keyIssueItemSchema)
                .build();

        // Esquema para la lista de oportunidades de mejora
        GeminiRequestDto.ResponseSchema improvementOpportunityItemSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("STRING")
                .build();
        GeminiRequestDto.ResponseSchema improvementOpportunitiesSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("ARRAY")
                .items(improvementOpportunityItemSchema)
                .build();

        // Esquema para el sentimiento general (enum)
        GeminiRequestDto.ResponseSchema overallSentimentSchema = GeminiRequestDto.ResponseSchema.builder()
                .type("STRING")
                .enumValues(List.of("POSITIVE", "NEGATIVE", "NEUTRAL"))
                .build();

        // Esquema para el resumen ejecutivo
        GeminiRequestDto.ResponseSchema executiveSummarySchema = GeminiRequestDto.ResponseSchema.builder()
                .type("STRING")
                .build();

        // Esquema del objeto raíz
        return GeminiRequestDto.ResponseSchema.builder()
                .type("OBJECT")
                .properties(Map.of(
                        "overallSentiment", overallSentimentSchema,
                        "executiveSummary", executiveSummarySchema,
                        "keyIssues", keyIssuesSchema,
                        "improvementOpportunities", improvementOpportunitiesSchema,
                        "sentimentDistribution", sentimentDistributionSchema
                ))
                .required(List.of(
                        "overallSentiment",
                        "executiveSummary",
                        "keyIssues",
                        "improvementOpportunities",
                        "sentimentDistribution"
                ))
                .build();
    }
}
