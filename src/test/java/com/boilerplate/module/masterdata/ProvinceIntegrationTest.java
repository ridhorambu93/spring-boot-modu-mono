package com.boilerplate.module.masterdata;

import com.boilerplate.module.masterdata.dto.ProvinceRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProvinceIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired ProvinceRepository provinceRepository;

    @BeforeEach
    void setUp() {
        provinceRepository.deleteAll();
    }

    @Test
    void findAll_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/v1/masterdata/provinces"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_success() throws Exception {
        var request = new ProvinceRequest("Jawa Barat", "JB");
        mockMvc.perform(post("/api/v1/masterdata/provinces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("Jawa Barat"))
            .andExpect(jsonPath("$.data.code").value("JB"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void create_duplicateCode_returnsConflict() throws Exception {
        var request = new ProvinceRequest("Jawa Barat", "JB");
        mockMvc.perform(post("/api/v1/masterdata/provinces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/masterdata/provinces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProvinceRequest("Jawa Barat Dua", "JB"))))
            .andExpect(status().isConflict());
    }

    @Test
    void create_unauthenticated_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/v1/masterdata/provinces")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new ProvinceRequest("Jawa Barat", "JB"))))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void delete_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/v1/masterdata/provinces/00000000-0000-0000-0000-000000000000"))
            .andExpect(status().isNotFound());
    }
}
