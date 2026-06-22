package com.opinai.service;

import com.opinai.controller.dto.AuthResponse;
import com.opinai.controller.dto.LoginRequest;
import com.opinai.controller.dto.RegisterRequest;
import com.opinai.controller.dto.UserSummaryDto;

public interface AuthService {
    UserSummaryDto register(RegisterRequest registerRequest);
    AuthResponse login(LoginRequest loginRequest);
    UserSummaryDto getCurrentUser();
}
