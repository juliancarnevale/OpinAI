package com.opinai.listener;

import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.model.*;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.AnalysisService;
import com.opinai.service.GeminiService;
import com.opinai.service.dto.GeminiAnalysisResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/opinai",
        "spring.datasource.username=opinai_user",
        "spring.datasource.password=opinai_password"
})
class AnalysisEventListenerIntegrationTest {

    @Autowired
    private AnalysisService analysisService;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private GeminiService geminiService;

    private User testUser;
    private Project testProject;
    private final String userEmail = "integration@opinai.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email(userEmail)
                .passwordHash("securePass123")
                .role(Role.ROLE_USER)
                .build();
        testUser = userRepository.save(testUser);

        testProject = Project.builder()
                .name("Proyecto de Integración")
                .user(testUser)
                .build();
        testProject = projectRepository.save(testProject);
    }

    @AfterEach
    void tearDown() {
        analysisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testFullAsyncAnalysisWorkflow() throws Exception {
        // Given
        CreateAnalysisRequest.FeedbackItemCreateRequest feedbackItemReq = 
                new CreateAnalysisRequest.FeedbackItemCreateRequest("El soporte técnico fue increíble, muy rápido.");
        CreateAnalysisRequest request = CreateAnalysisRequest.builder()
                .title("Análisis Integración")
                .feedbackItems(List.of(feedbackItemReq))
                .build();

        GeminiAnalysisResult mockResult = GeminiAnalysisResult.builder()
                .overallSentiment(SentimentType.POSITIVE)
                .executiveSummary("Soporte rápido y eficiente.")
                .keyIssues(Collections.emptyList())
                .improvementOpportunities(List.of("Mantener el nivel de atención"))
                .sentimentDistribution(new SentimentDistribution(1, 0, 0))
                .build();

        when(geminiService.analyzeComments(anyList())).thenReturn(mockResult);

        // When (hilo principal crea el análisis y publica el evento al confirmar la transacción)
        var response = analysisService.createAnalysis(testProject.getId(), request, userEmail);
        UUID analysisId = response.getId();

        // Entonces la respuesta inmediata debe estar en PENDING
        assertNotNull(analysisId);
        assertEquals(AnalysisStatus.PENDING, response.getStatus());

        // Esperar a que el procesamiento asíncrono se ejecute en el TaskExecutor en segundo plano (máximo 5 segundos)
        long startTime = System.currentTimeMillis();
        Analysis processedAnalysis = null;
        while (System.currentTimeMillis() - startTime < 5000) {
            processedAnalysis = analysisRepository.findById(analysisId).orElse(null);
            if (processedAnalysis != null && processedAnalysis.getStatus() == AnalysisStatus.COMPLETED) {
                break;
            }
            Thread.sleep(100);
        }

        // Verificar el estado final después de la ejecución asíncrona
        assertNotNull(processedAnalysis);
        assertEquals(AnalysisStatus.COMPLETED, processedAnalysis.getStatus());
        assertEquals(SentimentType.POSITIVE, processedAnalysis.getOverallSentiment());
        assertEquals("Soporte rápido y eficiente.", processedAnalysis.getExecutiveSummary());
        assertNotNull(processedAnalysis.getSentimentDistribution());
        assertEquals(1, processedAnalysis.getSentimentDistribution().getPositive());
        assertEquals(0, processedAnalysis.getSentimentDistribution().getNegative());
        assertEquals(0, processedAnalysis.getSentimentDistribution().getNeutral());
    }
}
