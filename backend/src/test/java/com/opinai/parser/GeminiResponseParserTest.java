package com.opinai.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.exception.GeminiParsingException;
import com.opinai.model.SentimentType;
import com.opinai.service.dto.GeminiAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeminiResponseParserTest {

    private GeminiResponseParser parser;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        parser = new GeminiResponseParser(objectMapper);
    }

    @Test
    void parse_WithValidJson_ShouldReturnResult() {
        String json = "{\n" +
                "  \"overallSentiment\": \"POSITIVE\",\n" +
                "  \"executiveSummary\": \"Excelente servicio en general.\",\n" +
                "  \"keyIssues\": [],\n" +
                "  \"improvementOpportunities\": [\"Mantener el nivel de atención\"],\n" +
                "  \"sentimentDistribution\": {\n" +
                "    \"positive\": 2,\n" +
                "    \"negative\": 0,\n" +
                "    \"neutral\": 0\n" +
                "  }\n" +
                "}";

        GeminiAnalysisResult result = parser.parse(json);

        assertNotNull(result);
        assertEquals(SentimentType.POSITIVE, result.getOverallSentiment());
        assertEquals("Excelente servicio en general.", result.getExecutiveSummary());
        assertTrue(result.getKeyIssues().isEmpty());
        assertEquals(1, result.getImprovementOpportunities().size());
        assertEquals("Mantener el nivel de atención", result.getImprovementOpportunities().get(0));
        assertEquals(2, result.getSentimentDistribution().getPositive());
        assertEquals(0, result.getSentimentDistribution().getNegative());
        assertEquals(0, result.getSentimentDistribution().getNeutral());
    }

    @Test
    void parse_WrappedInMarkdownCodeBlock_ShouldCleanAndParse() {
        String json = "```json\n" +
                "{\n" +
                "  \"overallSentiment\": \"NEUTRAL\",\n" +
                "  \"executiveSummary\": \"Comentarios mixtos.\",\n" +
                "  \"keyIssues\": [\"Demora en soporte\"],\n" +
                "  \"improvementOpportunities\": [],\n" +
                "  \"sentimentDistribution\": {\n" +
                "    \"positive\": 1,\n" +
                "    \"negative\": 1,\n" +
                "    \"neutral\": 1\n" +
                "  }\n" +
                "}\n" +
                "```";

        GeminiAnalysisResult result = parser.parse(json);

        assertNotNull(result);
        assertEquals(SentimentType.NEUTRAL, result.getOverallSentiment());
        assertEquals("Comentarios mixtos.", result.getExecutiveSummary());
        assertEquals(1, result.getKeyIssues().size());
        assertEquals("Demora en soporte", result.getKeyIssues().get(0));
        assertTrue(result.getImprovementOpportunities().isEmpty());
        assertEquals(1, result.getSentimentDistribution().getPositive());
        assertEquals(1, result.getSentimentDistribution().getNegative());
        assertEquals(1, result.getSentimentDistribution().getNeutral());
    }

    @Test
    void parse_WithNullInput_ShouldThrowGeminiParsingException() {
        GeminiParsingException exception = assertThrows(
                GeminiParsingException.class,
                () -> parser.parse(null)
        );
        assertTrue(exception.getMessage().contains("nula"));
    }

    @Test
    void parse_WithMalformedJson_ShouldThrowGeminiParsingException() {
        String invalidJson = "{\n" +
                "  \"overallSentiment\": \"POSITIVE\",\n" +
                "  \"executiveSummary\": \"Incompleto\""; // Falta cerrar llaves

        assertThrows(
                GeminiParsingException.class,
                () -> parser.parse(invalidJson)
        );
    }
}
