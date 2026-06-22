package com.opinai.listener;

import com.opinai.event.AnalysisProcessingRequestedEvent;
import com.opinai.model.Analysis;
import com.opinai.model.AnalysisStatus;
import com.opinai.model.FeedbackItem;
import com.opinai.repository.AnalysisRepository;
import com.opinai.service.GeminiService;
import com.opinai.service.dto.GeminiAnalysisResult;
import com.opinai.model.SentimentDistribution;
import com.opinai.model.SentimentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisEventListenerTest {

    @Mock
    private GeminiService geminiService;

    @Mock
    private AnalysisRepository analysisRepository;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private AnalysisEventListener listener;

    private UUID analysisId;
    private Analysis analysis;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        analysisId = UUID.randomUUID();
        analysis = new Analysis();
        analysis.setId(analysisId);
        analysis.setStatus(AnalysisStatus.PENDING);

        FeedbackItem item = new FeedbackItem();
        item.setContent("Me gusta la app");
        analysis.setFeedbackItems(List.of(item));

        // Mock del comportamiento de TransactionTemplate para que ejecute los callbacks inmediatamente
        lenient().when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback = invocation.getArgument(0);
                    return callback.doInTransaction(new SimpleTransactionStatus());
                });

        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(new SimpleTransactionStatus());
            return null;
        }).when(transactionTemplate).executeWithoutResult(any(Consumer.class));
    }

    @Test
    void onAnalysisProcessingRequested_Success_ShouldTransitionToCompleted() {
        // Given
        AnalysisProcessingRequestedEvent event = new AnalysisProcessingRequestedEvent(analysisId);
        GeminiAnalysisResult geminiResult = GeminiAnalysisResult.builder()
                .overallSentiment(SentimentType.POSITIVE)
                .executiveSummary("Muy positiva")
                .keyIssues(List.of())
                .improvementOpportunities(List.of("Mantener el rumbo"))
                .sentimentDistribution(new SentimentDistribution(1, 0, 0))
                .build();

        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));
        when(geminiService.analyzeComments(anyList())).thenReturn(geminiResult);

        // When
        listener.onAnalysisProcessingRequested(event);

        // Then
        assertEquals(AnalysisStatus.COMPLETED, analysis.getStatus());
        assertEquals(SentimentType.POSITIVE, analysis.getOverallSentiment());
        assertEquals("Muy positiva", analysis.getExecutiveSummary());
        assertTrue(analysis.getKeyIssues().isEmpty());
        assertEquals(1, analysis.getImprovementOpportunities().size());
        assertEquals(1, analysis.getSentimentDistribution().getPositive());
        
        verify(analysisRepository, atLeastOnce()).save(analysis);
    }

    @Test
    void onAnalysisProcessingRequested_NotPending_ShouldAbort() {
        // Given
        analysis.setStatus(AnalysisStatus.PROCESSING); // Ya no está en PENDING
        AnalysisProcessingRequestedEvent event = new AnalysisProcessingRequestedEvent(analysisId);

        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));

        // When
        listener.onAnalysisProcessingRequested(event);

        // Then
        assertEquals(AnalysisStatus.PROCESSING, analysis.getStatus());
        verify(geminiService, never()).analyzeComments(any());
        verify(analysisRepository, never()).save(any(Analysis.class));
    }

    @Test
    void onAnalysisProcessingRequested_NoComments_ShouldMarkAsFailed() {
        // Given
        analysis.setFeedbackItems(Collections.emptyList()); // Sin comentarios
        AnalysisProcessingRequestedEvent event = new AnalysisProcessingRequestedEvent(analysisId);

        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));

        // When
        listener.onAnalysisProcessingRequested(event);

        // Then
        assertEquals(AnalysisStatus.FAILED, analysis.getStatus());
        assertNull(analysis.getOverallSentiment());
        verify(geminiService, never()).analyzeComments(any());
        verify(analysisRepository, atLeastOnce()).save(analysis);
    }

    @Test
    void onAnalysisProcessingRequested_GeminiException_ShouldMarkAsFailed() {
        // Given
        AnalysisProcessingRequestedEvent event = new AnalysisProcessingRequestedEvent(analysisId);

        when(analysisRepository.findById(analysisId)).thenReturn(Optional.of(analysis));
        when(geminiService.analyzeComments(anyList())).thenThrow(new RuntimeException("API caída"));

        // When
        listener.onAnalysisProcessingRequested(event);

        // Then
        assertEquals(AnalysisStatus.FAILED, analysis.getStatus());
        assertNull(analysis.getOverallSentiment());
        assertNull(analysis.getExecutiveSummary());
        assertNull(analysis.getKeyIssues());
        assertNull(analysis.getImprovementOpportunities());
        assertNull(analysis.getSentimentDistribution());
        
        verify(analysisRepository, atLeastOnce()).save(analysis);
    }
}
