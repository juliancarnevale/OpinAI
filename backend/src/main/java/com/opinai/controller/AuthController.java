package com.opinai.controller;

import com.opinai.controller.dto.AuthResponse;
import com.opinai.controller.dto.LoginRequest;
import com.opinai.controller.dto.RegisterRequest;
import com.opinai.controller.dto.UserSummaryDto;
import com.opinai.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro e inicio de sesión de usuarios")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Registrar un nuevo usuario en la plataforma")
    public ResponseEntity<UserSummaryDto> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserSummaryDto registeredUser = authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUser);
    }

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario y generar un token JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse authResponse = authService.login(loginRequest);
        return ResponseEntity.ok(authResponse);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener el perfil del usuario autenticado actual")
    public ResponseEntity<UserSummaryDto> getCurrentUser() {
        UserSummaryDto currentUser = authService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }
}
