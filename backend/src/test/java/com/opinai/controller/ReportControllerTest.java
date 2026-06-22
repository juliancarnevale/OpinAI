package com.opinai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.controller.dto.ReportResponse;
import com.opinai.model.ExportFormat;
import com.opinai.model.ReportStatus;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.ReportService;
import com.opinai.service.dto.ReportFileStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.junit.jupiter.api.Assertions.assertEquals;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReportService reportService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private Authentication principal;
    private ReportResponse reportResponsePdf;
    private ReportResponse reportResponseCsv;
    private String userEmail = "dev@opinai.com";
    private UUID projectId;
    private UUID reportId;

    @BeforeEach
    void setUp() {
        principal = new UsernamePasswordAuthenticationToken(userEmail, null);
        projectId = UUID.randomUUID();
        reportId = UUID.randomUUID();

        reportResponsePdf = ReportResponse.builder()
                .id(reportId)
                .projectId(projectId)
                .name("report_pdf_test.pdf")
                .format(ExportFormat.PDF)
                .fileSize(1024L)
                .status(ReportStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();

        reportResponseCsv = ReportResponse.builder()
                .id(UUID.randomUUID())
                .projectId(projectId)
                .name("report_csv_test.csv")
                .format(ExportFormat.CSV)
                .fileSize(512L)
                .status(ReportStatus.READY)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReport_Success_ReturnsCreated() throws Exception {
        // Given
        when(reportService.createReport(eq(projectId), eq(ExportFormat.PDF), eq(userEmail)))
                .thenReturn(reportResponsePdf);

        // When & Then
        mockMvc.perform(post("/projects/{projectId}/reports", projectId)
                        .param("format", "PDF")
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(reportId.toString()))
                .andExpect(jsonPath("$.projectId").value(projectId.toString()))
                .andExpect(jsonPath("$.name").value("report_pdf_test.pdf"))
                .andExpect(jsonPath("$.format").value("PDF"))
                .andExpect(jsonPath("$.status").value("READY"));

        verify(reportService, times(1)).createReport(eq(projectId), eq(ExportFormat.PDF), eq(userEmail));
    }

    @Test
    void getReportsForProject_Success_ReturnsOk() throws Exception {
        // Given
        when(reportService.getReportsForProject(eq(projectId), eq(userEmail)))
                .thenReturn(Collections.singletonList(reportResponsePdf));

        // When & Then
        mockMvc.perform(get("/projects/{projectId}/reports", projectId)
                        .principal(principal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(reportId.toString()))
                .andExpect(jsonPath("$[0].name").value("report_pdf_test.pdf"));

        verify(reportService, times(1)).getReportsForProject(eq(projectId), eq(userEmail));
    }

    @Test
    void downloadReport_Pdf_Success_ReturnsStreamingResponse() throws Exception {
        // Given
        byte[] pdfContent = "Fake PDF Binary Content".getBytes(StandardCharsets.UTF_8);
        ReportFileStream fileStream = new ReportFileStream(
                "report_pdf_test.pdf",
                ExportFormat.PDF,
                new ByteArrayInputStream(pdfContent)
        );

        when(reportService.getReportFileStream(eq(reportId), eq(userEmail)))
                .thenReturn(fileStream);

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/reports/{reportId}/download", reportId)
                        .principal(principal))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_pdf_test.pdf\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/pdf"));

        // StreamingResponseBody resolution
        byte[] responseBytes = mvcResult.getResponse().getContentAsByteArray();
        assertEquals("Fake PDF Binary Content", new String(responseBytes, StandardCharsets.UTF_8));

        verify(reportService, times(1)).getReportFileStream(eq(reportId), eq(userEmail));
    }

    @Test
    void downloadReport_Csv_Success_ReturnsStreamingResponse() throws Exception {
        // Given
        byte[] csvContent = "header1,header2\nval1,val2".getBytes(StandardCharsets.UTF_8);
        ReportFileStream fileStream = new ReportFileStream(
                "report_csv_test.csv",
                ExportFormat.CSV,
                new ByteArrayInputStream(csvContent)
        );

        when(reportService.getReportFileStream(eq(reportId), eq(userEmail)))
                .thenReturn(fileStream);

        // When & Then
        MvcResult mvcResult = mockMvc.perform(get("/reports/{reportId}/download", reportId)
                        .principal(principal))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report_csv_test.csv\""))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv"));

        byte[] responseBytes = mvcResult.getResponse().getContentAsByteArray();
        assertEquals("header1,header2\nval1,val2", new String(responseBytes, StandardCharsets.UTF_8));

        verify(reportService, times(1)).getReportFileStream(eq(reportId), eq(userEmail));
    }

    @Test
    void deleteReport_Success_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(reportService).deleteReport(eq(reportId), eq(userEmail));

        // When & Then
        mockMvc.perform(delete("/reports/{reportId}", reportId)
                        .with(csrf())
                        .principal(principal))
                .andExpect(status().isNoContent());

        verify(reportService, times(1)).deleteReport(eq(reportId), eq(userEmail));
    }
}
