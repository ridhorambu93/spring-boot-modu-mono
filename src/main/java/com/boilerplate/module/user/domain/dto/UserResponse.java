package com.boilerplate.module.user.domain.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String fullName,
    Role role,
    Status status,
    Instant createdAt
) {
    public enum Role {
        USER, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE, BLOCKED
    }
}
