package com.opinai.security;

import com.opinai.controller.dto.CreateAnalysisRequest;
import com.opinai.controller.dto.CreateProjectRequest;
import com.opinai.model.*;
import com.opinai.repository.AnalysisRepository;
import com.opinai.repository.ProjectRepository;
import com.opinai.repository.ReportRepository;
import com.opinai.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/opinai",
        "spring.datasource.username=opinai_user",
        "spring.datasource.password=opinai_password"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AnalysisRepository analysisRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String tokenA;
    private String tokenB;

    private Project projectA;
    private Analysis analysisA;
    private Report reportA;

    @BeforeEach
    void setUp() {
        // 1. Crear Inquilinos
        User tenantA = User.builder()
                .email("tenantA@opinai.com")
                .passwordHash("hashA")
                .role(Role.ROLE_USER)
                .build();
        tenantA = userRepository.save(tenantA);
        
        Authentication authA = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        tenantA.getEmail(), "", Collections.emptyList()
                ),
                null, Collections.emptyList()
        );
        tokenA = jwtTokenProvider.generateToken(authA);

        User tenantB = User.builder()
                .email("tenantB@opinai.com")
                .passwordHash("hashB")
                .role(Role.ROLE_USER)
                .build();
        tenantB = userRepository.save(tenantB);
        
        Authentication authB = new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(
                        tenantB.getEmail(), "", Collections.emptyList()
                ),
                null, Collections.emptyList()
        );
        tokenB = jwtTokenProvider.generateToken(authB);

        // 2. Crear Recursos de Tenant A
        projectA = Project.builder()
                .name("Proyecto Privado A")
                .user(tenantA)
                .build();
        projectA = projectRepository.save(projectA);

        analysisA = Analysis.builder()
                .project(projectA)
                .title("Análisis Confidencial A")
                .status(AnalysisStatus.COMPLETED)
                .overallSentiment(SentimentType.POSITIVE)
                .executiveSummary("Secreto industrial")
                .build();
        analysisA = analysisRepository.save(analysisA);

        reportA = Report.builder()
                .project(projectA)
                .name("Reporte Secreto A")
                .format(ExportFormat.PDF)
                .status(ReportStatus.READY)
                .storageKey("test-pdf-key")
                .fileSize(100L)
                .build();
        reportA = reportRepository.save(reportA);
    }

    @AfterEach
    void tearDown() {
        reportRepository.deleteAll();
        analysisRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testTenantIsolationAndAntiIdorControls() throws Exception {
        // --- 1. Aislamiento Multi-Tenant (Listar Proyectos) ---
        // Tenant A debe ver su proyecto
        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(projectA.getId().toString()));

        // Tenant B no debe ver el proyecto de Tenant A
        mockMvc.perform(get("/projects")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // --- 2. Acceso Cruzado a Proyectos (Anti-IDOR) ---
        // Tenant B intenta leer el proyecto de Tenant A directamente por UUID
        mockMvc.perform(get("/projects/" + projectA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound()); // Debe retornar 404 (oculta existencia del recurso)

        // --- 3. Acceso Cruzado a Análisis (Anti-IDOR) ---
        // Tenant B intenta leer detalles del análisis de Tenant A
        mockMvc.perform(get("/analyses/" + analysisA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Tenant B intenta crear un análisis en el proyecto de Tenant A
        mockMvc.perform(post("/projects/" + projectA.getId() + "/analyses")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"IDOR\",\"feedbackItems\":[{\"content\":\"comentario de prueba\"}]}"))
                .andExpect(status().isNotFound());

        // --- 4. Acceso Cruzado a Reportes (Anti-IDOR) ---
        // Tenant B intenta descargar el PDF del reporte de Tenant A
        mockMvc.perform(get("/reports/" + reportA.getId() + "/download")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());

        // Tenant B intenta borrar el reporte de Tenant A
        mockMvc.perform(delete("/reports/" + reportA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }
}
