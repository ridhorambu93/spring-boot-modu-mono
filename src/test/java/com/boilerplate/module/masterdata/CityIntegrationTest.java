package com.boilerplate.module.masterdata;

import com.boilerplate.module.masterdata.dto.CityRequest;
import com.boilerplate.module.masterdata.entity.Province;
import com.boilerplate.module.masterdata.repository.CityRepository;
import com.boilerplate.module.masterdata.repository.ProvinceRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CityIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired CityRepository cityRepository;
    @Autowired ProvinceRepository provinceRepository;

    private UUID provinceId;

    @BeforeEach
    void setUp() {
        cityRepository.deleteAll();
        provinceRepository.deleteAll();

        Province province = new Province();
        province.setName("Jawa Barat");
        province.setCode("JB");
        provinceId = provinceRepository.save(province).getId();
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/masterdata/cities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void findAll_filteredByProvince() throws Exception {
        mockMvc.perform(get("/api/v1/masterdata/cities").param("provinceId", provinceId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_success() throws Exception {
        var request = new CityRequest("Bandung", "BDG", provinceId);
        mockMvc.perform(post("/api/v1/masterdata/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Bandung"))
            .andExpect(jsonPath("$.data.code").value("BDG"))
            .andExpect(jsonPath("$.data.provinceId").value(provinceId.toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_provinceNotFound_returns404() throws Exception {
        var request = new CityRequest("Bandung", "BDG", UUID.randomUUID());
        mockMvc.perform(post("/api/v1/masterdata/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }

    @Test
    void create_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/masterdata/cities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CityRequest("Bandung", "BDG", provinceId))))
            .andExpect(status().isForbidden());
    }
}
