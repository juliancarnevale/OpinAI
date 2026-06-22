package com.opinai.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.controller.dto.LoginRequest;
import com.opinai.controller.dto.RegisterRequest;
import com.opinai.model.User;
import com.opinai.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/opinai",
        "spring.datasource.username=opinai_user",
        "spring.datasource.password=opinai_password"
})
@AutoConfigureMockMvc
class ApiContractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testAuthenticationSuccessContract() throws Exception {
        RegisterRequest registerReq = RegisterRequest.builder()
                .email("contract@opinai.com")
                .password("securePass123")
                .firstName("Contract")
                .lastName("Auditor")
                .build();

        // 1. Success Payload: Registro (HTTP 201 Created)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("contract@opinai.com"))
                .andExpect(jsonPath("$.firstName").value("Contract"))
                .andExpect(jsonPath("$.lastName").value("Auditor"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.role", notNullValue()));

        LoginRequest loginReq = LoginRequest.builder()
                .email("contract@opinai.com")
                .password("securePass123")
                .build();

        // 2. Success Payload: Login (HTTP 200 OK)
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"));
    }

    @Test
    void testValidationErrorContract() throws Exception {
        // Enviar payload de registro vacío o inválido (HTTP 400 Bad Request)
        RegisterRequest invalidReq = RegisterRequest.builder()
                .email("invalid-email") // Email mal formado
                .password("") // Vacío (debe fallar validación de tamaño/not blank)
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Error de validación en los datos de entrada"))
                .andExpect(jsonPath("$.timestamp", notNullValue()))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errors[*].field", hasItem(either(is("email")).or(is("password")))));
    }

    @Test
    void testAuthenticationFailureContract() throws Exception {
        // 1. Rechazo con credenciales erróneas (HTTP 401 Unauthorized)
        LoginRequest badLogin = LoginRequest.builder()
                .email("wrong@opinai.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));

        // 2. Acceso a ruta protegida sin token (HTTP 401 Unauthorized)
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}
