package com.boilerplate.module.masterdata.mapper;

import com.boilerplate.module.masterdata.dto.CityResponse;
import com.boilerplate.module.masterdata.dto.ProvinceResponse;
import com.boilerplate.module.masterdata.entity.City;
import com.boilerplate.module.masterdata.entity.Province;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MasterdataMapper {

    ProvinceResponse toProvinceResponse(Province province);

    @Mapping(source = "province.id", target = "provinceId")
    @Mapping(source = "province.name", target = "provinceName")
    CityResponse toCityResponse(City city);
}
