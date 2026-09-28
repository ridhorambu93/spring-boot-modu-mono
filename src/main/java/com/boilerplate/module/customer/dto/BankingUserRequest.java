package com.boilerplate.module.customer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BankingUserRequest(
    @NotBlank @Size(max = 255) String fullName,
    @NotBlank @Email @Size(max = 255) String email,
    @NotBlank @Size(max = 100) String identityNumber,
    @Size(max = 30)
    @Pattern(regexp = "^\\+?[0-9 ()-]*$", message = "phoneNumber contains invalid characters")
    String phoneNumber
) {}
