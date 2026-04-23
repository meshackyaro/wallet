package com.koins.wallet.services;

import com.koins.wallet.entities.Transaction;
import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.enums.TransactionStatus;
import com.koins.wallet.enums.TransactionType;
import com.koins.wallet.repositories.TransactionRepository;
import com.koins.wallet.repositories.UserRepository;
import com.koins.wallet.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public Wallet getWalletByUser(String email) {
        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));
    }

    public List<Transaction> getTransactionHistory(String email) {
        User user = userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return transactionRepository.findByUserId(user.getId());
    }

    @Transactional
    public Transaction initiateFunding(String email, BigDecimal amount) {
        Wallet wallet = getWalletByUser(email);
        String reference = java.util.UUID.randomUUID().toString();

        Transaction transaction = Transaction.builder()
                .user(wallet.getUser())
                .wallet(wallet)
                .transactionType(TransactionType.CREDIT)
                .amount(amount)
                .transactionStatus(TransactionStatus.PENDING)
                .referenceNumber(reference)
                .build();

        return transactionRepository.save(transaction);
    }

    @Transactional
    public void fundWallet(String email, BigDecimal amount, String reference) {
        Transaction transaction = transactionRepository.findByReferenceNumber(reference)
                .orElse(null);

        if (transaction == null) {
            // Unsolicited credit (not initiated by our system) or mock, create new if needed
            // For simplicity, we just create a new one if it doesn't exist
            Wallet wallet = getWalletByUser(email);
            transaction = Transaction.builder()
                    .user(wallet.getUser())
                    .wallet(wallet)
                    .transactionType(TransactionType.CREDIT)
                    .amount(amount)
                    .transactionStatus(TransactionStatus.SUCCESS)
                    .referenceNumber(reference)
                    .build();
            wallet.setBalance(wallet.getBalance().add(amount));
            walletRepository.save(wallet);
            transactionRepository.save(transaction);
            return;
        }

        if (transaction.getTransactionStatus() == TransactionStatus.SUCCESS) {
            return; // Already processed
        }

        Wallet wallet = transaction.getWallet();
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        transaction.setTransactionStatus(TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);
    }
}
