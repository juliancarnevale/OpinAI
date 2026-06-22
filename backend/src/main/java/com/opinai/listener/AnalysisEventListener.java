package com.opinai.listener;

import com.opinai.event.AnalysisProcessingRequestedEvent;
import com.opinai.model.Analysis;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.FeedbackItem;
import com.opinai.repository.AnalysisRepository;
import com.opinai.service.GeminiService;
import com.opinai.service.dto.GeminiAnalysisResult;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AnalysisEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AnalysisEventListener.class);

    private final GeminiService geminiService;
    private final AnalysisRepository analysisRepository;
    private final TransactionTemplate transactionTemplate;

    @Async("analysisTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAnalysisProcessingRequested(AnalysisProcessingRequestedEvent event) {
        UUID analysisId = event.getAnalysisId();
        logger.info("Inicio de procesamiento asíncrono para el análisis: {}", analysisId);

        // Transacción 1 (Corta): Cambiar estado a PROCESSING
        Boolean isPending = transactionTemplate.execute(status -> {
            Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
            if (analysis != null && analysis.getStatus() == AnalysisStatus.PENDING) {
                analysis.setStatus(AnalysisStatus.PROCESSING);
                analysisRepository.save(analysis);
                logger.info("Análisis {} cambiado a estado PROCESSING", analysisId);
                return true;
            }
            return false;
        });

        if (Boolean.FALSE.equals(isPending)) {
            logger.warn("El análisis {} no se encuentra en estado PENDING. Abortando procesamiento asíncrono.", analysisId);
            return;
        }

        // Obtener opiniones asociadas dentro de un contexto transaccional separado de sólo lectura
        List<String> comments = transactionTemplate.execute(status -> {
            Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
            if (analysis != null) {
                return analysis.getFeedbackItems().stream()
                        .map(FeedbackItem::getContent)
                        .toList();
            }
            return Collections.emptyList();
        });

        if (comments == null || comments.isEmpty()) {
            logger.warn("El análisis {} no contiene opiniones para evaluar.", analysisId);
            markAsFailed(analysisId, "El análisis no contiene opiniones para evaluar");
            return;
        }

        try {
            logger.info("Iniciando llamada externa a Gemini API para el análisis {} con {} comentarios", analysisId, comments.size());
            // Llamada bloqueante HTTP fuera de transacciones de base de datos
            GeminiAnalysisResult result = geminiService.analyzeComments(comments);
            logger.info("Respuesta de Gemini recibida con éxito para el análisis {}", analysisId);

            // Transacción 2 (Corta): Guardar resultados del análisis y cambiar a COMPLETED
            transactionTemplate.executeWithoutResult(status -> {
                Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
                if (analysis != null) {
                    analysis.setOverallSentiment(result.getOverallSentiment());
                    analysis.setExecutiveSummary(result.getExecutiveSummary());
                    analysis.setKeyIssues(result.getKeyIssues());
                    analysis.setImprovementOpportunities(result.getImprovementOpportunities());
                    analysis.setSentimentDistribution(result.getSentimentDistribution());
                    analysis.setStatus(AnalysisStatus.COMPLETED);
                    analysisRepository.save(analysis);
                    logger.info("Análisis {} completado y guardado en base de datos", analysisId);
                }
            });

        } catch (Exception e) {
            logger.error("Error crítico procesando análisis {} con Gemini API: {}", analysisId, e.getMessage(), e);
            markAsFailed(analysisId, e.getMessage());
        }
    }

    private void markAsFailed(UUID analysisId, String reason) {
        // Transacción 3 (Corta): Marcar estado como FAILED ante excepciones
        transactionTemplate.executeWithoutResult(status -> {
            Analysis analysis = analysisRepository.findById(analysisId).orElse(null);
            if (analysis != null) {
                analysis.setStatus(AnalysisStatus.FAILED);
                // Limpiar cualquier resultado parcial obsoleto
                analysis.setOverallSentiment(null);
                analysis.setExecutiveSummary(null);
                analysis.setKeyIssues(null);
                analysis.setImprovementOpportunities(null);
                analysis.setSentimentDistribution(null);
                analysisRepository.save(analysis);
                logger.info("Análisis {} marcado como FAILED debido a: {}", analysisId, reason);
            }
        });
    }
}
