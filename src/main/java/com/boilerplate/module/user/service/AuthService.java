package com.boilerplate.module.user.service;

import com.boilerplate.module.user.domain.dto.AuthResponse;
import com.boilerplate.module.user.domain.dto.LoginRequest;
import com.boilerplate.module.user.domain.dto.RefreshTokenRequest;
import com.boilerplate.module.user.domain.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
}
