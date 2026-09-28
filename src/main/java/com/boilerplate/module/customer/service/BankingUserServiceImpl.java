package com.boilerplate.module.customer.service;

import com.boilerplate.module.customer.dto.BankingUserRequest;
import com.boilerplate.module.customer.dto.BankingUserResponse;
import com.boilerplate.module.customer.entity.BankingUser;
import com.boilerplate.module.customer.repository.BankingUserRepository;
import com.boilerplate.shared.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BankingUserServiceImpl implements BankingUserService {

    private final BankingUserRepository bankingUserRepository;

    @Override
    @Transactional(readOnly = true)
    public List<BankingUserResponse> findAll() {
        return bankingUserRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BankingUserResponse findById(UUID id) {
        return toResponse(findActiveById(id));
    }

    @Override
    @Transactional
    public BankingUserResponse create(BankingUserRequest request) {
        if (bankingUserRepository.existsByEmailAndDeletedAtIsNull(request.email())) {
            throw AppException.conflict("Customer email already exists");
        }
        if (bankingUserRepository.existsByIdentityNumberAndDeletedAtIsNull(request.identityNumber())) {
            throw AppException.conflict("Customer identity number already exists");
        }

        BankingUser bankingUser = new BankingUser();
        applyRequest(bankingUser, request);
        return toResponse(bankingUserRepository.save(bankingUser));
    }

    @Override
    @Transactional
    public BankingUserResponse update(UUID id, BankingUserRequest request) {
        BankingUser bankingUser = findActiveById(id);

        if (bankingUserRepository.existsByEmailAndIdNotAndDeletedAtIsNull(request.email(), id)) {
            throw AppException.conflict("Customer email already exists");
        }
        if (bankingUserRepository.existsByIdentityNumberAndIdNotAndDeletedAtIsNull(
            request.identityNumber(), id
        )) {
            throw AppException.conflict("Customer identity number already exists");
        }

        applyRequest(bankingUser, request);
        return toResponse(bankingUserRepository.save(bankingUser));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        BankingUser bankingUser = findActiveById(id);
        bankingUser.setDeletedAt(Instant.now());
        bankingUserRepository.save(bankingUser);
    }

    private BankingUser findActiveById(UUID id) {
        return bankingUserRepository.findByIdAndDeletedAtIsNull(id)
            .orElseThrow(() -> AppException.notFound("Customer not found"));
    }

    private void applyRequest(BankingUser bankingUser, BankingUserRequest request) {
        bankingUser.setFullName(request.fullName());
        bankingUser.setEmail(request.email());
        bankingUser.setIdentityNumber(request.identityNumber());
        bankingUser.setPhoneNumber(request.phoneNumber());
    }

    private BankingUserResponse toResponse(BankingUser bankingUser) {
        return new BankingUserResponse(
            bankingUser.getId(),
            bankingUser.getFullName(),
            bankingUser.getEmail(),
            bankingUser.getIdentityNumber(),
            bankingUser.getPhoneNumber(),
            // valueOf get from Enum DTO
            BankingUserResponse.Status.valueOf(bankingUser.getStatus().name()),
            bankingUser.getCreatedAt(),
            bankingUser.getUpdatedAt()
        );
    }
}
