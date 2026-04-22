package com.koins.wallet.repositories;

import com.koins.wallet.entities.Loan;
import com.koins.wallet.entities.User;
import com.koins.wallet.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByUserId(UUID userId);
    List<Loan> findByStatusAndDueDateBefore(LoanStatus status, LocalDateTime date);
}
