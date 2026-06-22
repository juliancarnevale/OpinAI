package com.opinai.service;

import com.opinai.controller.dto.ReportResponse;
import com.opinai.exception.BusinessException;
import com.opinai.exception.ReportGenerationException;
import com.opinai.exception.ResourceNotFoundException;
import com.opinai.model.*;
import com.opinai.repository.*;
import com.opinai.service.dto.*;
import com.opinai.service.impl.ReportServiceImpl;
import com.opinai.service.mapper.ReportPayloadAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FeedbackItemRepository feedbackItemRepository;
    @Mock
    private AnalysisRepository analysisRepository;
    @Mock
    private ReportGeneratorService reportGeneratorService;
    @Mock
    private StorageService storageService;
    @Mock
    private ReportPayloadAssembler reportPayloadAssembler;

    @InjectMocks
    private ReportServiceImpl reportService;

    private User currentUser;
    private Project myProject;
    private String userEmail = "test@opinai.com";
    private UUID projectId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .id(UUID.randomUUID())
                .email(userEmail)
                .role(Role.ROLE_USER)
                .build();

        myProject = Project.builder()
                .id(projectId)
                .name("Café Central")
                .description("Descripción")
                .user(currentUser)
                .build();
    }

    @Test
    void createReport_Success() {
        // Given
        ExportFormat format = ExportFormat.PDF;
        byte[] fakePdfBytes = "%PDF-1.4 Fake Bytes".getBytes();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.of(myProject));
        when(feedbackItemRepository.countByAnalysisProject(myProject)).thenReturn(150L);

        // Mock del guardado inicial (Status: GENERATING)
        Report mockGeneratingReport = Report.builder()
                .id(UUID.randomUUID())
                .project(myProject)
                .name("opinai-reporte-cafe-central.pdf")
                .format(format)
                .storageKey("reports/fakeKey.pdf")
                .fileSize(0L)
                .status(ReportStatus.GENERATING)
                .build();

        when(reportRepository.save(any(Report.class))).thenAnswer(invocation -> {
            Report r = invocation.getArgument(0);
            if (r.getId() == null) {
                r.setId(UUID.randomUUID());
            }
            return r;
        });

        List<Analysis> analyses = Collections.emptyList();
        when(analysisRepository.findByProjectAndStatusWithFeedbacks(myProject, AnalysisStatus.COMPLETED)).thenReturn(analyses);

        ProjectReportPayload payload = ProjectReportPayload.builder()
                .projectName(myProject.getName())
                .projectName(myProject.getDescription())
                .analyses(Collections.emptyList())
                .feedbackItems(Collections.emptyList())
                .build();
        when(reportPayloadAssembler.assemble(myProject, analyses)).thenReturn(payload);
        when(reportGeneratorService.generate(payload, format)).thenReturn(fakePdfBytes);
        when(storageService.store(anyString(), eq(fakePdfBytes))).thenReturn("reports/fakeKey.pdf");

        // When
        ReportResponse response = reportService.createReport(projectId, format, userEmail);

        // Then
        assertNotNull(response);
        assertEquals(ReportStatus.READY, response.getStatus());
        assertEquals(fakePdfBytes.length, response.getFileSize());
        verify(reportRepository, atLeastOnce()).save(any(Report.class));
        verify(storageService, times(1)).store(anyString(), eq(fakePdfBytes));
    }

    @Test
    void createReport_ThrowsBusinessException_WhenFeedbackLimitExceeded() {
        // Given
        ExportFormat format = ExportFormat.PDF;
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.of(myProject));
        when(feedbackItemRepository.countByAnalysisProject(myProject)).thenReturn(5500L); // Excede 5000

        // When & Then
        assertThrows(BusinessException.class, () -> 
                reportService.createReport(projectId, format, userEmail));

        verify(reportRepository, never()).save(any());
        verify(reportGeneratorService, never()).generate(any(), any());
    }

    @Test
    void createReport_ThrowsResourceNotFoundException_WhenNotOwner() {
        // Given
        ExportFormat format = ExportFormat.PDF;
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.findByIdAndUser(projectId, currentUser)).thenReturn(Optional.empty()); // No es dueño

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> 
                reportService.createReport(projectId, format, userEmail));

        verify(feedbackItemRepository, never()).countByAnalysisProject(any());
        verify(reportRepository, never()).save(any());
    }

    @Test
    void getReportsForProject_Success() {
        // Given
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(currentUser));
        when(projectRepository.existsByIdAndUser(projectId, currentUser)).thenReturn(true);
        
        Report report = Report.builder()
                .id(UUID.randomUUID())
                .project(myProject)
                .name("reporte.pdf")
                .format(ExportFormat.PDF)
                .storageKey("key.pdf")
                .fileSize(100L)
                .status(ReportStatus.READY)
                .build();
        when(reportRepository.findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc(projectId, userEmail))
                .thenReturn(Collections.singletonList(report));

        // When
        List<ReportResponse> results = reportService.getReportsForProject(projectId, userEmail);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("reporte.pdf", results.get(0).getName());
    }

    @Test
    void deleteReport_Success() {
        // Given
        UUID reportId = UUID.randomUUID();
        Report report = Report.builder()
                .id(reportId)
                .project(myProject)
                .name("reporte.pdf")
                .format(ExportFormat.PDF)
                .storageKey("key.pdf")
                .fileSize(100L)
                .status(ReportStatus.READY)
                .build();

        when(reportRepository.findByIdAndProjectUserEmail(reportId, userEmail)).thenReturn(Optional.of(report));

        // When
        reportService.deleteReport(reportId, userEmail);

        // Then
        verify(reportRepository, times(1)).delete(report);
    }
}
