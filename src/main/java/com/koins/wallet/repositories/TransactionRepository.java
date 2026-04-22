package com.koins.wallet.repositories;

import com.koins.wallet.entities.Transaction;
import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByUserId(UUID userId);
    boolean existsByReferenceNumber(String referenceNumber);
    Optional<Transaction> findByReferenceNumber(String referenceNumber);
}
