package com.boilerplate.module.user.controller;

import com.boilerplate.module.user.domain.dto.UserResponse;
import com.boilerplate.module.user.service.UserService;
import com.boilerplate.shared.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(id)));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> me(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.ok(userService.findById(userId)));
    }
}
