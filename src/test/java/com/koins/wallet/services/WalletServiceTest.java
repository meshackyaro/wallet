package com.koins.wallet.services;

import com.koins.wallet.entities.Transaction;
import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.enums.Role;
import com.koins.wallet.enums.TransactionStatus;
import com.koins.wallet.repositories.TransactionRepository;
import com.koins.wallet.repositories.UserRepository;
import com.koins.wallet.repositories.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WalletService walletService;

    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(UUID.randomUUID())
                .emailAddress("test@test.com")
                .role(Role.USER)
                .build();

        wallet = Wallet.builder()
                .id(UUID.randomUUID())
                .user(user)
                .balance(new BigDecimal("0.00"))
                .build();
    }

    @Test
    void fundWallet_Success() {
        when(transactionRepository.findByReferenceNumber("REF-123")).thenReturn(Optional.empty());
        when(userRepository.findByEmailAddress("test@test.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        walletService.fundWallet("test@test.com", new BigDecimal("500.00"), "REF-123");

        assertEquals(new BigDecimal("500.00"), wallet.getBalance());
        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void fundWallet_DuplicateReference_DoesNothing() {
        Transaction existingTx = Transaction.builder()
                .transactionStatus(TransactionStatus.SUCCESS)
                .build();
        when(transactionRepository.findByReferenceNumber("REF-123")).thenReturn(Optional.of(existingTx));

        walletService.fundWallet("test@test.com", new BigDecimal("500.00"), "REF-123");

        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
