package com.boilerplate.module.customer.dto;

import java.time.Instant;
import java.util.UUID;


public record BankingUserResponse(
    UUID id,
    String fullName,
    String email,
    String identityNumber,
    String phoneNumber,
    Status status,
    Instant createdAt,
    Instant updatedAt
) {

    public enum Status {
        ACTIVE, INACTIVE, BLOCKED

    }
}
