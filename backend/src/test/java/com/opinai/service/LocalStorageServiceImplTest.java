package com.opinai.service;

import com.opinai.exception.ResourceNotFoundException;
import com.opinai.service.impl.LocalStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceImplTest {

    private LocalStorageServiceImpl storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageServiceImpl();
        // Inyectar el directorio temporal de pruebas
        ReflectionTestUtils.setField(storageService, "localDir", tempDir.toString());
        storageService.init();
    }

    @Test
    void storeAndRetrieve_Success() throws IOException {
        // Given
        String key = "test-reports/report-123.pdf";
        byte[] content = "Contenido de prueba con ñ y áéíóú".getBytes(StandardCharsets.UTF_8);

        // When
        String savedKey = storageService.store(key, content);

        // Then
        assertEquals(key, savedKey);
        
        // Recuperar
        InputStream is = storageService.retrieve(key);
        assertNotNull(is);
        
        byte[] retrievedContent;
        try (is) {
            retrievedContent = is.readAllBytes();
        }
        
        assertArrayEquals(content, retrievedContent);
        String retrievedText = new String(retrievedContent, StandardCharsets.UTF_8);
        assertTrue(retrievedText.contains("ñ"));
        assertTrue(retrievedText.contains("áéíóú"));
    }

    @Test
    void delete_Success() {
        // Given
        String key = "test-reports/delete-me.txt";
        byte[] content = "Borrar".getBytes(StandardCharsets.UTF_8);
        storageService.store(key, content);

        // When
        storageService.delete(key);

        // Then
        assertThrows(ResourceNotFoundException.class, () -> storageService.retrieve(key));
    }

    @Test
    void retrieve_ThrowsNotFound_WhenFileDoesNotExist() {
        // Given
        String key = "non-existent-file.pdf";

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> storageService.retrieve(key));
    }

    @Test
    void directoryTraversal_ThrowsSecurityException() {
        // Given
        String maliciousKey = "../outside-folder.pdf";
        byte[] content = "Malicioso".getBytes(StandardCharsets.UTF_8);

        // When & Then for store
        assertThrows(SecurityException.class, () -> storageService.store(maliciousKey, content));

        // When & Then for retrieve
        assertThrows(SecurityException.class, () -> storageService.retrieve(maliciousKey));

        // When & Then for delete
        assertThrows(SecurityException.class, () -> storageService.delete(maliciousKey));
    }
}
