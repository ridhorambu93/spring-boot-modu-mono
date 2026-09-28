package com.boilerplate.module.customer.dto;

import com.boilerplate.module.customer.entity.BankingUser;

import java.time.Instant;
import java.util.UUID;

public record BankingUserResponse(
    UUID id,
    String fullName,
    String email,
    String identityNumber,
    String phoneNumber,
    BankingUser.Status status,
    Instant createdAt,
    Instant updatedAt
) {}
