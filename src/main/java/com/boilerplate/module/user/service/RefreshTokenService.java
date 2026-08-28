package com.boilerplate.module.user.service;

import com.boilerplate.module.user.entity.RefreshToken;
import com.boilerplate.module.user.entity.User;

public interface RefreshTokenService {
    RefreshToken create(User user);
    RefreshToken validate(String token);
    void deleteByUser(User user);
}
