package com.boilerplate.module.masterdata.service;

import com.boilerplate.module.masterdata.api.ProvinceApi;
import com.boilerplate.module.masterdata.dto.ProvinceRequest;
import com.boilerplate.module.masterdata.dto.ProvinceResponse;
import com.boilerplate.module.masterdata.entity.Province;
import com.boilerplate.module.masterdata.mapper.MasterdataMapper;
import com.boilerplate.module.masterdata.repository.CityRepository;
import com.boilerplate.module.masterdata.repository.ProvinceRepository;
import com.boilerplate.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProvinceServiceImpl implements ProvinceService, ProvinceApi {

    @Override
    public boolean existsById(UUID id) {
        return provinceRepository.findByIdAndDeletedAtIsNull(id).isPresent();
    }

    private final ProvinceRepository provinceRepository;
    private final CityRepository cityRepository;
    private final MasterdataMapper masterdataMapper;

    @Override
    public List<ProvinceResponse> findAll() {
        return provinceRepository.findAllByDeletedAtIsNull()
            .stream().map(masterdataMapper::toProvinceResponse).toList();
    }

    @Override
    public ProvinceResponse findById(UUID id) {
        return provinceRepository.findByIdAndDeletedAtIsNull(id)
            .map(masterdataMapper::toProvinceResponse)
            .orElseThrow(() -> AppException.notFound("Province not found"));
    }

    @Override
    public ProvinceResponse create(ProvinceRequest request) {
        if (provinceRepository.existsByCodeAndDeletedAtIsNull(request.code()))
            throw AppException.conflict("Province code already exists");

        Province province = new Province();
        province.setName(request.name());
        province.setCode(request.code());
        return masterdataMapper.toProvinceResponse(provinceRepository.save(province));
    }

    @Override
    public ProvinceResponse update(UUID id, ProvinceRequest request) {
        Province province = provinceRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> AppException.notFound("Province not found"));

        province.setName(request.name());
        province.setCode(request.code());
        return masterdataMapper.toProvinceResponse(provinceRepository.save(province));
    }

    @Override
    public void delete(UUID id) {
        Province province = provinceRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> AppException.notFound("Province not found"));
        if (cityRepository.existsByProvinceIdAndDeletedAtIsNull(id))
            throw AppException.badRequest("Province still has active cities");
        province.setDeletedAt(Instant.now());
        provinceRepository.save(province);
    }
}
