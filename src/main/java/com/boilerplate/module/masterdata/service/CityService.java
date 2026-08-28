package com.boilerplate.module.masterdata.service;

import com.boilerplate.module.masterdata.dto.CityRequest;
import com.boilerplate.module.masterdata.dto.CityResponse;

import java.util.List;
import java.util.UUID;

public interface CityService {
    List<CityResponse> findAll();
    List<CityResponse> findByProvince(UUID provinceId);
    CityResponse findById(UUID id);
    CityResponse create(CityRequest request);
    CityResponse update(UUID id, CityRequest request);
    void delete(UUID id);
}
