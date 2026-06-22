package com.opinai.service;

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
import com.opinai.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User user;
    private UserSummaryDto userSummaryDto;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .build();

        loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.ROLE_USER)
                .build();

        userSummaryDto = UserSummaryDto.builder()
                .id(user.getId())
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role("ROLE_USER")
                .build();
    }

    @Test
    void register_Success() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserSummaryDto(user)).thenReturn(userSummaryDto);

        // When
        UserSummaryDto result = authService.register(registerRequest);

        // Then
        assertNotNull(result);
        assertEquals(registerRequest.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void register_ThrowsEmailAlreadyExistsException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_Success() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("jwtToken");
        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(userMapper.toUserSummaryDto(user)).thenReturn(userSummaryDto);

        // When
        AuthResponse result = authService.login(loginRequest);

        // Then
        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        assertEquals("Bearer", result.getTokenType());
        assertEquals(userSummaryDto.getEmail(), result.getUser().getEmail());
    }

    @Test
    void login_ThrowsInvalidCredentialsException_OnBadCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // When & Then
        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginRequest));
    }
}
