package com.opinai.service.impl;

import com.opinai.controller.dto.ReportResponse;
import com.opinai.exception.BusinessException;
import com.opinai.exception.ReportGenerationException;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.*;
import com.opinai.repository.*;
import com.opinai.service.*;
import com.opinai.service.dto.*;
import com.opinai.service.mapper.ReportPayloadAssembler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final FeedbackItemRepository feedbackItemRepository;
    private final AnalysisRepository analysisRepository;
    private final ReportGeneratorService reportGeneratorService;
    private final StorageService storageService;
    private final ReportPayloadAssembler reportPayloadAssembler;

    @Override
    @Transactional
    public ReportResponse createReport(UUID projectId, ExportFormat format, String userEmail) {
        // 1. Validar ownership del proyecto (Anti-IDOR)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));
        Project project = projectRepository.findByIdAndUser(projectId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado o sin acceso para el usuario"));

        // 2. Validar límite de negocio de 5000 feedbacks
        long feedbackCount = feedbackItemRepository.countByAnalysisProject(project);
        if (feedbackCount > 5000) {
            throw new BusinessException("El proyecto supera el límite de 5000 opiniones permitido para exportación.");
        }

        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmm"));
        String cleanProjectName = project.getName().toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-");
        String userFacingName = String.format("opinai-reporte-%s-%s.%s", 
                cleanProjectName, timestamp, format.name().toLowerCase());

        UUID reportId = UUID.randomUUID();
        String storageKey = String.format("reports/%s/%s.%s", 
                projectId, reportId, format.name().toLowerCase());

        Report report = Report.builder()
                .id(reportId)
                .project(project)
                .name(userFacingName)
                .format(format)
                .storageKey(storageKey)
                .fileSize(0L)
                .status(ReportStatus.GENERATING)
                .build();

        report = reportRepository.save(report);

        // 5. Registrar hook de rollback para limpiar el almacenamiento físico si la transacción falla
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCompletion(int status) {
                    if (status == STATUS_ROLLED_BACK) {
                        storageService.delete(storageKey);
                    }
                }
            });
        }

        try {
            // 6. Cargar análisis y comentarios con query JOIN FETCH optimizada
            List<Analysis> analyses = analysisRepository.findByProjectAndStatusWithFeedbacks(project, AnalysisStatus.COMPLETED);

            ProjectReportPayload payload = reportPayloadAssembler.assemble(project, analyses);

            byte[] fileContent = reportGeneratorService.generate(payload, format);

            storageService.store(storageKey, fileContent);

            report.setStatus(ReportStatus.READY);
            report.setFileSize(fileContent.length);
            report = reportRepository.save(report);

            return mapToResponse(report);

        } catch (Exception e) {
            throw new ReportGenerationException("Error al generar el archivo de reporte", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsForProject(UUID projectId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + userEmail));
        if (!projectRepository.existsByIdAndUser(projectId, user)) {
            throw new ResourceNotFoundException("Proyecto no encontrado o sin acceso");
        }

        return reportRepository.findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc(projectId, userEmail)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteReport(UUID reportId, String userEmail) {
        // Buscar el reporte validando ownership en una única consulta JPQL (Anti-IDOR)
        Report report = reportRepository.findByIdAndProjectUserEmail(reportId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado o sin acceso"));

        String keyToDelete = report.getStorageKey();

        reportRepository.delete(report);

        // Registrar hook para eliminar el archivo físico del storage tras confirmarse el commit
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    storageService.delete(keyToDelete);
                }
            });
        } else {
            storageService.delete(keyToDelete);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportFileStream getReportFileStream(UUID reportId, String userEmail) {
        // Buscar el reporte validando ownership (Anti-IDOR)
        Report report = reportRepository.findByIdAndProjectUserEmail(reportId, userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reporte no encontrado o sin acceso"));

        if (report.getStatus() != ReportStatus.READY) {
            throw new BusinessException("El reporte solicitado no está listo para descarga");
        }

        return new ReportFileStream(
                report.getName(),
                report.getFormat(),
                storageService.retrieve(report.getStorageKey())
        );
    }

    private ReportResponse mapToResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .projectId(report.getProject().getId())
                .name(report.getName())
                .format(report.getFormat())
                .fileSize(report.getFileSize())
                .status(report.getStatus())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
