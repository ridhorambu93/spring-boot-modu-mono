package com.boilerplate.module.customer.controller;

import com.boilerplate.module.customer.dto.BankingUserRequest;
import com.boilerplate.module.customer.dto.BankingUserResponse;
import com.boilerplate.module.customer.service.BankingUserService;
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

@Tag(name = "Banking Customers")
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class BankingUserController {

    private final BankingUserService bankingUserService;

    @Operation(summary = "Get all banking customers")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BankingUserResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(bankingUserService.findAll()));
    }

    @Operation(summary = "Get banking customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BankingUserResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(bankingUserService.findById(id)));
    }

    @Operation(summary = "Create banking customer")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankingUserResponse>> create(
        @Valid @RequestBody BankingUserRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(bankingUserService.create(request)));
    }

    @Operation(summary = "Update banking customer")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BankingUserResponse>> update(
        @PathVariable UUID id,
        @Valid @RequestBody BankingUserRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(bankingUserService.update(id, request)));
    }

    @Operation(summary = "Soft delete banking customer")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bankingUserService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
