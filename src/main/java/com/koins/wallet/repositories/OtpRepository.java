package com.koins.wallet.repositories;

import com.koins.wallet.entities.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findTopByEmailOrderByExpiresAtDesc(String email);
    void deleteByEmail(String email);
}
