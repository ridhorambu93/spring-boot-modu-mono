package com.boilerplate.module.masterdata.dto;

import java.util.UUID;

public record CityResponse(UUID id, String name, String code, UUID provinceId, String provinceName) {}
