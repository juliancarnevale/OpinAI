package com.opinai.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.opinai.controller.dto.AuthResponse;
import com.opinai.controller.dto.LoginRequest;
import com.opinai.controller.dto.RegisterRequest;
import com.opinai.controller.dto.UserSummaryDto;
import com.opinai.exception.InvalidCredentialsException;
import com.opinai.security.CustomUserDetailsService;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private UserSummaryDto userSummaryDto;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("newuser@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        userSummaryDto = UserSummaryDto.builder()
                .id(UUID.randomUUID())
                .email("newuser@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("ROLE_USER")
                .build();

        authResponse = AuthResponse.builder()
                .token("mockedJwtToken")
                .user(userSummaryDto)
                .build();
    }

    @Test
    void register_Success_ReturnsCreated() throws Exception {
        // Given
        when(authService.register(any(RegisterRequest.class))).thenReturn(userSummaryDto);

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
    }

    @Test
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        registerRequest.setEmail("invalid-email-format");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("email"))
                .andExpect(jsonPath("$.errors[0].message").value("El email debe tener un formato válido"));
    }

    @Test
    void register_EmptyPassword_ReturnsBadRequest() throws Exception {
        // Given
        registerRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void login_Success_ReturnsOk() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mockedJwtToken"))
                .andExpect(jsonPath("$.user.email").value("newuser@example.com"));
    }

    @Test
    void login_InvalidCredentials_ReturnsUnauthorized() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException("Correo electrónico o contraseña incorrectos."));

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Correo electrónico o contraseña incorrectos."));
    }
}
