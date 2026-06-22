package com.opinai.service.impl;

import com.opinai.controller.dto.AnalysisDetailResponse;
import com.opinai.controller.dto.AnalysisResponse;
import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.event.AnalysisProcessingRequestedEvent;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.mapper.AnalysisMapper;
import com.opinai.model.*;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.UserRepository;
import com.opinai.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final AnalysisRepository analysisRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AnalysisMapper analysisMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public AnalysisResponse createAnalysis(UUID projectId, CreateAnalysisRequest request, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));

        Analysis analysis = new Analysis();
        analysis.setTitle(request.getTitle());
        analysis.setProject(project);
        analysis.setStatus(AnalysisStatus.PENDING);

        List<FeedbackItem> feedbackItems = request.getFeedbackItems().stream()
                .map(itemReq -> {
                    FeedbackItem item = new FeedbackItem();
                    item.setContent(itemReq.getContent());
                    item.setSourceType(FeedbackSourceType.MANUAL);
                    item.setAnalysis(analysis);
                    return item;
                })
                .toList();

        analysis.setFeedbackItems(feedbackItems);

        Analysis savedAnalysis = analysisRepository.save(analysis);

        // Publicar evento para procesamiento asíncrono
        eventPublisher.publishEvent(new AnalysisProcessingRequestedEvent(savedAnalysis.getId()));

        return analysisMapper.toResponse(savedAnalysis);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnalysisResponse> getAnalysesForProject(UUID projectId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso"));

        List<Analysis> analyses = analysisRepository.findByProjectOrderByCreatedAtDesc(project);
        return analysisMapper.toResponseList(analyses);
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisDetailResponse getAnalysisById(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Análisis no encontrado o sin acceso"));

        return analysisMapper.toDetailResponse(analysis);
    }

    @Override
    @Transactional
    public void deleteAnalysis(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);
        Analysis analysis = analysisRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Análisis no encontrado o sin acceso"));

        analysisRepository.delete(analysis);
    }

    @Override
    @Transactional
    public AnalysisResponse reprocessAnalysis(UUID id, String userEmail) {
        User user = getUserByEmail(userEmail);

        // Intentar la actualización de forma atómica en la base de datos
        int updatedRows = analysisRepository.reprocessAtomic(id, user);

        if (updatedRows == 0) {
            // Si no se actualizó, investigamos por qué ocurrió el fallo (para dar excepciones precisas)
            Analysis analysis = analysisRepository.findById(id).orElse(null);
            if (analysis == null || !analysis.getProject().getUser().equals(user)) {
                throw new ResourceNotFoundException("Análisis no encontrado o sin acceso");
            }
            if (analysis.getStatus() == AnalysisStatus.PROCESSING) {
                throw new IllegalStateException("El análisis ya se encuentra en procesamiento");
            }
            if (analysis.getStatus() == AnalysisStatus.COMPLETED) {
                throw new IllegalStateException("El análisis ya ha sido completado y no requiere reprocesamiento");
            }
            if (analysis.getStatus() == AnalysisStatus.PENDING) {
                throw new IllegalStateException("El análisis ya está en cola para procesamiento");
            }
        }

        // Recuperar el análisis ya transicionado en base de datos para mapearlo al DTO
        Analysis updatedAnalysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Análisis no encontrado o sin acceso"));

        // Publicar evento para reprocesamiento asíncrono
        eventPublisher.publishEvent(new AnalysisProcessingRequestedEvent(updatedAnalysis.getId()));

        return analysisMapper.toResponse(updatedAnalysis);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }
}
