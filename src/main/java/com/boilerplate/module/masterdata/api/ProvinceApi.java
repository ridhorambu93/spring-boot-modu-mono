package com.boilerplate.module.masterdata.api;

import com.boilerplate.module.masterdata.dto.ProvinceResponse;

import java.util.UUID;

public interface ProvinceApi {
    ProvinceResponse findById(UUID id);
    boolean existsById(UUID id);
}
