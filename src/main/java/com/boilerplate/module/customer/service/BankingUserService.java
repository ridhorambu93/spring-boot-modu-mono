package com.boilerplate.module.customer.service;

import com.boilerplate.module.customer.dto.BankingUserRequest;
import com.boilerplate.module.customer.dto.BankingUserResponse;

import java.util.List;
import java.util.UUID;

public interface BankingUserService {

    List<BankingUserResponse> findAll();

    BankingUserResponse findById(UUID id);

    BankingUserResponse create(BankingUserRequest request);

    BankingUserResponse update(UUID id, BankingUserRequest request);

    void delete(UUID id);
}
