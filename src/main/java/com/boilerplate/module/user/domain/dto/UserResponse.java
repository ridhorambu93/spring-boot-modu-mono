package com.boilerplate.module.user.domain.dto;

import com.boilerplate.module.user.entity.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    User.Role role,
    User.Status status,
    Instant createdAt
) {}
