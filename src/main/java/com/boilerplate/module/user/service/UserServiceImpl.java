package com.boilerplate.module.user.service;

import com.boilerplate.module.user.api.UserApi;
import com.boilerplate.module.user.domain.dto.UserResponse;
import com.boilerplate.module.user.mapper.UserMapper;
import com.boilerplate.module.user.repository.UserRepository;
import com.boilerplate.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserApi {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse findById(UUID id) {
        return userRepository.findById(id)
            .map(userMapper::toResponse)
            .orElseThrow(() -> AppException.notFound("User not found"));
    }

    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }
}
