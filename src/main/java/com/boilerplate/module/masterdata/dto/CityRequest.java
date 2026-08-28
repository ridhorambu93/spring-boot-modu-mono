package com.boilerplate.module.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CityRequest(
    @NotBlank String name,
    @NotBlank @Size(max = 10) String code,
    @NotNull UUID provinceId
) {}
