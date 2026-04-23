package com.koins.wallet.controllers;

import com.koins.wallet.dto.FundWalletRequest;
import com.koins.wallet.entities.Transaction;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.services.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<Wallet> getBalance(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(walletService.getWalletByUser(email));
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getTransactions(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(walletService.getTransactionHistory(email));
    }

    @org.springframework.web.bind.annotation.PostMapping("/fund")
    public ResponseEntity<Transaction> fundWallet(
            @Valid @RequestBody FundWalletRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(walletService.initiateFunding(email, request.getAmount()));
    }
}
