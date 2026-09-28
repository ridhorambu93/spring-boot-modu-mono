package com.boilerplate.module.customer.repository;

import com.boilerplate.module.customer.entity.BankingUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankingUserRepository extends JpaRepository<BankingUser, UUID> {

    List<BankingUser> findAllByDeletedAtIsNullOrderByCreatedAtDesc();

    Optional<BankingUser> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    boolean existsByIdentityNumberAndDeletedAtIsNull(String identityNumber);

    boolean existsByEmailAndIdNotAndDeletedAtIsNull(String email, UUID id);

    boolean existsByIdentityNumberAndIdNotAndDeletedAtIsNull(String identityNumber, UUID id);
}
