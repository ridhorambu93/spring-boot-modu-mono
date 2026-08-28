package com.boilerplate.module.masterdata.repository;

import com.boilerplate.module.masterdata.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProvinceRepository extends JpaRepository<Province, UUID> {
    List<Province> findAllByDeletedAtIsNull();
    Optional<Province> findByIdAndDeletedAtIsNull(UUID id);
    boolean existsByCodeAndDeletedAtIsNull(String code);
}
