package com.boilerplate.module.user.service;

import com.boilerplate.module.user.dto.AuthResponse;
import com.boilerplate.module.user.dto.LoginRequest;
import com.boilerplate.module.user.dto.RegisterRequest;
import com.boilerplate.module.user.dto.RefreshTokenRequest;
import com.boilerplate.module.user.entity.RefreshToken;
import com.boilerplate.module.user.entity.User;
import com.boilerplate.module.user.mapper.UserMapper;
import com.boilerplate.module.user.repository.UserRepository;
import com.boilerplate.infrastructure.security.JwtService;
import com.boilerplate.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw AppException.conflict("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> AppException.unauthorized("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw AppException.unauthorized("Invalid credentials");
        }

        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.refreshToken());
        User user = refreshToken.getUser();
        refreshTokenService.deleteByUser(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.refreshToken());
        refreshTokenService.deleteByUser(refreshToken.getUser());
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        RefreshToken refreshToken = refreshTokenService.create(user);
        return AuthResponse.of(accessToken, refreshToken.getToken(), userMapper.toResponse(user));
    }
}