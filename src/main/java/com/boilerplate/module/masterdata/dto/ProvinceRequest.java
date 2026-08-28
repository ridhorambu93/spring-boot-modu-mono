package com.boilerplate.module.masterdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProvinceRequest(
    @NotBlank String name,
    @NotBlank @Size(max = 10) String code
) {}
