package com.opinai.service.impl;

import com.opinai.exception.ResourceNotFoundException;
import com.opinai.service.StorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class LocalStorageServiceImpl implements StorageService {

    @Value("${opinai.storage.local-dir:./storage/reports}")
    private String localDir;

    private Path baseDirectory;

    @PostConstruct
    public void init() {
        try {
            this.baseDirectory = Paths.get(localDir).toAbsolutePath().normalize();
            Files.createDirectories(this.baseDirectory);
            log.info("Directorio de almacenamiento de reportes inicializado en: {}", this.baseDirectory);
        } catch (IOException e) {
            log.error("No se pudo crear el directorio de almacenamiento en: {}", localDir, e);
            throw new IllegalStateException("Error al inicializar almacenamiento local", e);
        }
    }

    @Override
    public String store(String key, byte[] content) {
        try {
            Path targetPath = baseDirectory.resolve(key).normalize();
            
            // Prevención de Directory Traversal
            if (!targetPath.startsWith(baseDirectory)) {
                throw new SecurityException("Intento de Directory Traversal detectado para la clave: " + key);
            }

            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, content);
            log.info("Archivo guardado con éxito en: {}", targetPath);
            return key;
        } catch (IOException e) {
            log.error("Error al escribir archivo en disco para la clave: {}", key, e);
            throw new RuntimeException("Error al guardar archivo en el almacenamiento", e);
        }
    }

    @Override
    public InputStream retrieve(String key) {
        try {
            Path targetPath = baseDirectory.resolve(key).normalize();
            
            // Prevención de Directory Traversal
            if (!targetPath.startsWith(baseDirectory)) {
                throw new SecurityException("Intento de Directory Traversal detectado para la clave: " + key);
            }

            if (!Files.exists(targetPath)) {
                throw new ResourceNotFoundException("El archivo solicitado no existe: " + key);
            }

            return Files.newInputStream(targetPath);
        } catch (IOException e) {
            log.error("Error al leer archivo en disco para la clave: {}", key, e);
            throw new RuntimeException("Error al recuperar archivo del almacenamiento", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path targetPath = baseDirectory.resolve(key).normalize();
            
            // Prevención de Directory Traversal
            if (!targetPath.startsWith(baseDirectory)) {
                throw new SecurityException("Intento de Directory Traversal detectado para la clave: " + key);
            }

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("Archivo eliminado de disco con éxito: {}", targetPath);
            } else {
                log.warn("Se intentó borrar el archivo pero no existe en disco: {}", targetPath);
            }
        } catch (IOException e) {
            log.error("Error al borrar archivo en disco para la clave: {}", key, e);
            throw new RuntimeException("Error al borrar archivo del almacenamiento", e);
        }
    }
}
