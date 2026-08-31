package com.boilerplate.module.masterdata.service;

import com.boilerplate.module.masterdata.api.CityApi;
import com.boilerplate.module.masterdata.dto.CityRequest;
import com.boilerplate.module.masterdata.dto.CityResponse;
import com.boilerplate.module.masterdata.entity.City;
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
public class CityServiceImpl implements CityService, CityApi {

    @Override
    public boolean existsById(UUID id) {
        return cityRepository.findByIdAndDeletedAtIsNull(id).isPresent();
    }

    private final CityRepository cityRepository;
    private final ProvinceRepository provinceRepository;
    private final MasterdataMapper masterdataMapper;

    @Override
    public List<CityResponse> findAll() {
        return cityRepository.findAllByDeletedAtIsNull()
            .stream().map(masterdataMapper::toCityResponse).toList();
    }

    @Override
    public List<CityResponse> findByProvince(UUID provinceId) {
        return cityRepository.findAllByProvinceIdAndDeletedAtIsNull(provinceId)
            .stream().map(masterdataMapper::toCityResponse).toList();
    }

    @Override
    public CityResponse findById(UUID id) {
        return cityRepository.findByIdAndDeletedAtIsNull(id)
            .map(masterdataMapper::toCityResponse)
            .orElseThrow(() -> AppException.notFound("City not found"));
    }

    @Override
    public CityResponse create(CityRequest request) {
        if (cityRepository.existsByCodeAndDeletedAtIsNull(request.code()))
            throw AppException.conflict("City code already exists");

        var province = provinceRepository.findByIdAndDeletedAtIsNull(request.provinceId())
            .orElseThrow(() -> AppException.notFound("Province not found"));

        City city = new City();
        city.setName(request.name());
        city.setCode(request.code());
        city.setProvince(province);
        return masterdataMapper.toCityResponse(cityRepository.save(city));
    }

    @Override
    public CityResponse update(UUID id, CityRequest request) {
        City city = cityRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> AppException.notFound("City not found"));

        var province = provinceRepository.findByIdAndDeletedAtIsNull(request.provinceId())
            .orElseThrow(() -> AppException.notFound("Province not found"));

        city.setName(request.name());
        city.setCode(request.code());
        city.setProvince(province);
        return masterdataMapper.toCityResponse(cityRepository.save(city));
    }

    @Override
    public void delete(UUID id) {
        City city = cityRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> AppException.notFound("City not found"));
        city.setDeletedAt(Instant.now());
        cityRepository.save(city);
    }
}
