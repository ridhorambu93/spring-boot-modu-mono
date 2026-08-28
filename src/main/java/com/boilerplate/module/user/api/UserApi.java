package com.boilerplate.module.user.api;

import com.boilerplate.module.user.domain.dto.UserResponse;

import java.util.UUID;

public interface UserApi {
    UserResponse findById(UUID id);
    boolean existsById(UUID id);
}
