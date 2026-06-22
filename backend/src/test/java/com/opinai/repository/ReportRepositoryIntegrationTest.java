package com.opinai.repository;

import com.opinai.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/opinai",
    "spring.datasource.username=opinai_user",
    "spring.datasource.password=opinai_password"
})
@Transactional
class ReportRepositoryIntegrationTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User owner;
    private User otherUser;
    private Project project;
    private Report report;

    @BeforeEach
    void setUp() {
        // Limpiar para asegurar reproducibilidad si es necesario
        reportRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Crear usuarios
        owner = User.builder()
                .email("propietario@opinai.com")
                .passwordHash("hash")
                .firstName("Propietario")
                .lastName("Test")
                .role(Role.ROLE_USER)
                .build();
        owner = userRepository.save(owner);

        otherUser = User.builder()
                .email("intruso@opinai.com")
                .passwordHash("hash")
                .firstName("Intruso")
                .lastName("Test")
                .role(Role.ROLE_USER)
                .build();
        otherUser = userRepository.save(otherUser);

        // 2. Crear proyecto para el propietario
        project = Project.builder()
                .user(owner)
                .name("Proyecto Privado")
                .description("Detalles")
                .build();
        project = projectRepository.save(project);

        // 3. Crear reporte asociado al proyecto
        report = Report.builder()
                .project(project)
                .name("opinai-reporte-privado.pdf")
                .format(ExportFormat.PDF)
                .storageKey("reports/private/report.pdf")
                .fileSize(2048L)
                .status(ReportStatus.READY)
                .build();
        report = reportRepository.save(report);
    }

    @Test
    void findByIdAndProjectUserEmail_ReturnsReport_WhenOwnerQueries() {
        // When
        Optional<Report> foundReport = reportRepository.findByIdAndProjectUserEmail(report.getId(), owner.getEmail());

        // Then
        assertTrue(foundReport.isPresent());
        assertEquals(report.getId(), foundReport.get().getId());
        assertEquals("opinai-reporte-privado.pdf", foundReport.get().getName());
    }

    @Test
    void findByIdAndProjectUserEmail_ReturnsEmpty_WhenIntruderQueries() {
        // When
        Optional<Report> foundReport = reportRepository.findByIdAndProjectUserEmail(report.getId(), otherUser.getEmail());

        // Then
        assertFalse(foundReport.isPresent(), "No se debe permitir acceder a un reporte ajeno (Anti-IDOR)");
    }

    @Test
    void findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc_ReturnsList_WhenOwnerQueries() {
        // When
        List<Report> reports = reportRepository.findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc(project.getId(), owner.getEmail());

        // Then
        assertNotNull(reports);
        assertEquals(1, reports.size());
        assertEquals(report.getId(), reports.get(0).getId());
    }

    @Test
    void findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc_ReturnsEmptyList_WhenIntruderQueries() {
        // When
        List<Report> reports = reportRepository.findByProjectIdAndProjectUserEmailOrderByCreatedAtDesc(project.getId(), otherUser.getEmail());

        // Then
        assertNotNull(reports);
        assertTrue(reports.isEmpty(), "No se deben listar reportes de proyectos ajenos (Anti-IDOR)");
    }

    @Test
    void findStorageKeysByProjectId_ReturnsKeysCorrectly() {
        // When
        List<String> keys = reportRepository.findStorageKeysByProjectId(project.getId());

        // Then
        assertNotNull(keys);
        assertEquals(1, keys.size());
        assertEquals("reports/private/report.pdf", keys.get(0));
    }
}
