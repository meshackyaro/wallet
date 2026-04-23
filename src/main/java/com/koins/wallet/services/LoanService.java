package com.koins.wallet.services;

import com.koins.wallet.dto.LoanRequest;
import com.koins.wallet.entities.Loan;
import com.koins.wallet.entities.Transaction;
import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.enums.LoanStatus;
import com.koins.wallet.enums.TransactionStatus;
import com.koins.wallet.enums.TransactionType;
import com.koins.wallet.repositories.LoanRepository;
import com.koins.wallet.repositories.TransactionRepository;
import com.koins.wallet.repositories.UserRepository;
import com.koins.wallet.repositories.WalletRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final EventPublisherService eventPublisherService;

    public List<Loan> getUserLoans(String email) {
        User user = getUser(email);
        return loanRepository.findByUserId(user.getId());
    }

    public Loan getLoanById(String email, UUID loanId) {
        User user = getUser(email);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (!loan.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized to view this loan");
        }
        return loan;
    }

    @Transactional
    public Loan applyForLoan(String email, LoanRequest request) {
        User user = getUser(email);
        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Cannot apply for loan with empty wallet");
        }

        BigDecimal maxLoanAmount = wallet.getBalance().multiply(new BigDecimal("3"));
        if (request.getAmount().compareTo(maxLoanAmount) > 0) {
            throw new IllegalArgumentException("Loan amount cannot exceed 3x wallet balance");
        }

        Loan loan = Loan.builder()
                .user(user)
                .amount(request.getAmount())
                .interestRate(new BigDecimal("5.0")) // Fixed 5% interest
                .durationDays(request.getDurationDays())
                .status(LoanStatus.PENDING)
                .build();

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan approveLoan(UUID loanId) { // Typically an admin endpoint
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalArgumentException("Loan is not pending");
        }

        loan.setStatus(LoanStatus.APPROVED);
        loanRepository.save(loan);

        // Publish to RabbitMQ
        eventPublisherService.publishLoanApprovalEvent(loan.getUser().getEmailAddress(), loan.getId().toString());

        return loan;
    }

    @Transactional
    public Loan disburseLoan(UUID loanId) { // Admin endpoint
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalArgumentException("Loan must be approved before disbursement");
        }

        Wallet wallet = walletRepository.findByUser(loan.getUser())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        // Update Wallet
        wallet.setBalance(wallet.getBalance().add(loan.getAmount()));
        walletRepository.save(wallet);

        // Create Transaction
        Transaction transaction = Transaction.builder()
                .user(loan.getUser())
                .wallet(wallet)
                .transactionType(TransactionType.LOAN_DISBURSEMENT)
                .amount(loan.getAmount())
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber("DISB-" + UUID.randomUUID())
                .build();
        transactionRepository.save(transaction);

        // Update Loan
        loan.setStatus(LoanStatus.DISBURSED);
        loan.setDueDate(LocalDateTime.now().plusDays(loan.getDurationDays()));

        // Generate Repayment Schedule
        BigDecimal totalRepayment = loan.getAmount().multiply(BigDecimal.ONE.add(loan.getInterestRate().divide(new BigDecimal("100"))));
        String schedule = String.format("[{\"dueDate\": \"%s\", \"amount\": %s, \"status\": \"PENDING\"}]",
                loan.getDueDate().toString(), totalRepayment.toString());
        loan.setRepaymentSchedule(schedule);

        Loan savedLoan = loanRepository.save(loan);

        // Publish to RabbitMQ
        eventPublisherService.publishLoanDisbursementEvent(loan.getUser().getEmailAddress(), loan.getId().toString(), loan.getAmount().toString());

        return savedLoan;
    }

    @Transactional
    public Loan repayLoan(String email, UUID loanId) {
        User user = getUser(email);
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        BigDecimal amountToRepay = getAmountToRepay(loan, user);

        Wallet wallet = walletRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found"));

        if (wallet.getBalance().compareTo(amountToRepay) < 0) {
            throw new IllegalArgumentException("Insufficient wallet balance to repay loan");
        }

        // Deduct from wallet
        wallet.setBalance(wallet.getBalance().subtract(amountToRepay));
        walletRepository.save(wallet);

        // Create Transaction
        Transaction transaction = Transaction.builder()
                .user(user)
                .wallet(wallet)
                .transactionType(TransactionType.REPAYMENT)
                .amount(amountToRepay)
                .transactionStatus(TransactionStatus.SUCCESS)
                .referenceNumber("REP-" + UUID.randomUUID())
                .build();
        transactionRepository.save(transaction);

        // Update Loan
        loan.setStatus(LoanStatus.REPAID);
        // Mark schedule as repaid
        if (loan.getRepaymentSchedule() != null) {
            loan.setRepaymentSchedule(loan.getRepaymentSchedule().replace("\"status\": \"PENDING\"", "\"status\": \"REPAID\""));
        }
        Loan savedLoan = loanRepository.save(loan);

        // Publish to RabbitMQ
        eventPublisherService.publishLoanRepaymentEvent(user.getEmailAddress(), loan.getId().toString(), amountToRepay.toString());

        return savedLoan;
    }

    private static BigDecimal getAmountToRepay(Loan loan, User user) {
        if (!loan.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized");
        }

        if (loan.getStatus() != LoanStatus.DISBURSED && loan.getStatus() != LoanStatus.DEFAULTED) {
            throw new IllegalArgumentException("Loan is not active or already repaid");
        }

        // Calculate total amount to repay (principal + interest)
        BigDecimal interestMultiplier = BigDecimal.ONE.add(loan.getInterestRate().divide(new BigDecimal("100")));
        return loan.getAmount().multiply(interestMultiplier);
    }

    private User getUser(String email) {
        return userRepository.findByEmailAddress(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
}
