package com.boilerplate.module.masterdata.service;

import com.boilerplate.module.masterdata.dto.ProvinceRequest;
import com.boilerplate.module.masterdata.dto.ProvinceResponse;

import java.util.List;
import java.util.UUID;

public interface ProvinceService {
    List<ProvinceResponse> findAll();
    ProvinceResponse findById(UUID id);
    ProvinceResponse create(ProvinceRequest request);
    ProvinceResponse update(UUID id, ProvinceRequest request);
    void delete(UUID id);
}
