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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5433/opinai",
        "spring.datasource.username=opinai_user",
        "spring.datasource.password=opinai_password"
})
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterLoginAndAccessWorkflow() throws Exception {
        String email = "test@opinai.com";
        String password = "securePassword123";

        // 1. Registro de Usuario
        RegisterRequest registerReq = RegisterRequest.builder()
                .email(email)
                .password(password)
                .firstName("Test")
                .lastName("User")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.firstName").value("Test"))
                .andExpect(jsonPath("$.lastName").value("User"));

        // Verificar persistencia y hash de contraseña
        User savedUser = userRepository.findByEmail(email).orElse(null);
        assertNotNull(savedUser);
        assertTrue(passwordEncoder.matches(password, savedUser.getPasswordHash()));

        // 2. Inicio de Sesión
        LoginRequest loginReq = LoginRequest.builder()
                .email(email)
                .password(password)
                .build();

        String responseContent = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn().getResponse().getContentAsString();

        // Extraer token
        String token = objectMapper.readTree(responseContent).get("token").asText();
        assertNotNull(token);

        // 3. Acceso a Endpoint Protegido con Token Válido
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        // 4. Rechazo sin Token
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());

        // 5. Rechazo con Token Inválido / Alterado
        mockMvc.perform(get("/auth/me")
                        .header("Authorization", "Bearer " + token + "invalidated"))
                .andExpect(status().isUnauthorized());
    }
}
