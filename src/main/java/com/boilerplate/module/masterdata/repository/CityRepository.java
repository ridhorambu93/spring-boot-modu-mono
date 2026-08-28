package com.boilerplate.module.masterdata.repository;

import com.boilerplate.module.masterdata.entity.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CityRepository extends JpaRepository<City, UUID> {
    List<City> findAllByDeletedAtIsNull();
    List<City> findAllByProvinceIdAndDeletedAtIsNull(UUID provinceId);
    Optional<City> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByCodeAndDeletedAtIsNull(String code);
}
