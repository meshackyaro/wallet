package com.koins.wallet.controllers;

import com.koins.wallet.dto.LoanRequest;
import com.koins.wallet.entities.Loan;
import com.koins.wallet.services.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    @PostMapping("/apply")
    public ResponseEntity<Loan> applyForLoan(@Valid @RequestBody LoanRequest request, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loanService.applyForLoan(email, request));
    }

    @GetMapping
    public ResponseEntity<List<Loan>> getUserLoans(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loanService.getUserLoans(email));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Loan> getLoanById(@PathVariable UUID id, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loanService.getLoanById(email, id));
    }

    @PostMapping("/{id}/repay")
    public ResponseEntity<Loan> repayLoan(@PathVariable UUID id, Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(loanService.repayLoan(email, id));
    }

    // Admin endpoints
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Loan> approveLoan(@PathVariable UUID id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }

    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Loan> disburseLoan(@PathVariable UUID id) {
        return ResponseEntity.ok(loanService.disburseLoan(id));
    }
}
