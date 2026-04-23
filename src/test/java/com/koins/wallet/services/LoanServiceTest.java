package com.koins.wallet.services;

import com.koins.wallet.dto.LoanRequest;
import com.koins.wallet.entities.Loan;
import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.enums.Role;
import com.koins.wallet.repositories.LoanRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private EventPublisherService eventPublisherService;

    @InjectMocks
    private LoanService loanService;

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
                .balance(new BigDecimal("1000.00"))
                .build();
    }

    @Test
    void applyForLoan_Success() {
        LoanRequest request = new LoanRequest();
        request.setAmount(new BigDecimal("2500.00")); // <= 3000
        request.setDurationDays(30);

        when(userRepository.findByEmailAddress("test@test.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));
        when(loanRepository.save(any(Loan.class))).thenAnswer(i -> i.getArguments()[0]);

        Loan loan = loanService.applyForLoan("test@test.com", request);

        assertNotNull(loan);
        assertEquals(new BigDecimal("2500.00"), loan.getAmount());
    }

    @Test
    void applyForLoan_Exceeds3xBalance_ThrowsException() {
        LoanRequest request = new LoanRequest();
        request.setAmount(new BigDecimal("3500.00")); // > 3000
        request.setDurationDays(30);

        when(userRepository.findByEmailAddress("test@test.com")).thenReturn(Optional.of(user));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThrows(IllegalArgumentException.class, () -> loanService.applyForLoan("test@test.com", request));
    }
}
