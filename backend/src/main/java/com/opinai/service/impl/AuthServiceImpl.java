package com.opinai.service.impl;

import com.opinai.controller.dto.AuthResponse;
import com.opinai.controller.dto.LoginRequest;
import com.opinai.controller.dto.RegisterRequest;
import com.opinai.controller.dto.UserSummaryDto;
import com.opinai.exception.EmailAlreadyExistsException;
import com.opinai.exception.InvalidCredentialsException;
import com.opinai.mapper.UserMapper;
import com.opinai.model.Role;
import com.opinai.model.User;
import com.opinai.repository.UserRepository;
import com.opinai.security.JwtTokenProvider;
import com.opinai.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserSummaryDto register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new EmailAlreadyExistsException("El correo electrónico " + registerRequest.getEmail() + " ya está registrado.");
        }

        User user = userMapper.toEntity(registerRequest);
        user.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_USER); // Rol de usuario estándar por defecto

        User savedUser = userRepository.save(user);
        return userMapper.toUserSummaryDto(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Credenciales inválidas"));

            return AuthResponse.builder()
                    .token(jwt)
                    .user(userMapper.toUserSummaryDto(user))
                    .build();

        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Correo electrónico o contraseña incorrectos.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryDto getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new InvalidCredentialsException("Usuario no autenticado.");
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Usuario no encontrado."));
                
        return userMapper.toUserSummaryDto(user);
    }
}
