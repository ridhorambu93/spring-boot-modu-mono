package com.boilerplate.module.masterdata.api;

import com.boilerplate.module.masterdata.dto.CityResponse;

import java.util.UUID;

public interface CityApi {
    CityResponse findById(UUID id);
    boolean existsById(UUID id);
}
