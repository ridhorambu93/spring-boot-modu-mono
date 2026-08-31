package com.boilerplate.module.masterdata.controller;

import com.boilerplate.module.masterdata.dto.CityRequest;
import com.boilerplate.module.masterdata.dto.CityResponse;
import com.boilerplate.module.masterdata.service.CityService;
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

@Tag(name = "Masterdata - City")
@RestController
@RequestMapping("/api/v1/masterdata/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(summary = "Get all cities, optionally filtered by provinceId")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CityResponse>>> findAll(
        @RequestParam(required = false) UUID provinceId
    ) {
        var result = provinceId != null
            ? cityService.findByProvince(provinceId)
            : cityService.findAll();
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @Operation(summary = "Get city by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CityResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(cityService.findById(id)));
    }

    @Operation(summary = "Create city (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> create(@Valid @RequestBody CityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(cityService.create(request)));
    }

    @Operation(summary = "Update city (Admin only)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CityResponse>> update(@PathVariable UUID id, @Valid @RequestBody CityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(cityService.update(id, request)));
    }

    @Operation(summary = "Delete city (Admin only)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
