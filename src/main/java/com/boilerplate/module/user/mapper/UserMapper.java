package com.boilerplate.module.user.mapper;

import com.boilerplate.module.user.dto.UserResponse;
import com.boilerplate.module.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}