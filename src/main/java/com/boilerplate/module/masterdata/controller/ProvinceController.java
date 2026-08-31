package com.boilerplate.module.masterdata.controller;

import com.boilerplate.module.masterdata.dto.ProvinceRequest;
import com.boilerplate.module.masterdata.dto.ProvinceResponse;
import com.boilerplate.module.masterdata.service.ProvinceService;
import com.boilerplate.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Masterdata - Province")
@RestController
@RequestMapping("/api/v1/masterdata/provinces")
@RequiredArgsConstructor
public class ProvinceController {

    private final ProvinceService provinceService;

    @Operation(summary = "Get all provinces")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProvinceResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(provinceService.findAll()));
    }

    @Operation(summary = "Get province by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProvinceResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(provinceService.findById(id)));
    }

    @Operation(summary = "Create province (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProvinceResponse>> create(@Valid @RequestBody ProvinceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(provinceService.create(request)));
    }

    @Operation(summary = "Update province (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProvinceResponse>> update(@PathVariable UUID id, @Valid @RequestBody ProvinceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(provinceService.update(id, request)));
    }

    @Operation(summary = "Delete province (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        provinceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
