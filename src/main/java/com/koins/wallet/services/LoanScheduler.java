package com.koins.wallet.services;

import com.koins.wallet.entities.Loan;
import com.koins.wallet.enums.LoanStatus;
import com.koins.wallet.repositories.LoanRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoanScheduler {
    private final LoanRepository loanRepository;

    // Run every day at midnight
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void markOverdueLoans() {
        log.info("Running scheduled job to mark overdue loans...");
        List<Loan> activeLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.DISBURSED, LocalDateTime.now());

        for (Loan loan : activeLoans) {
            loan.setStatus(LoanStatus.DEFAULTED);
            loanRepository.save(loan);
            log.info("Loan {} marked as DEFAULTED due to overdue payment.", loan.getId());
        }
    }

    // Run every day at 8 AM
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendRepaymentReminders() {
        log.info("Running scheduled job to send repayment reminders...");
        // Look for loans due in 3 days
        LocalDateTime targetDate = LocalDateTime.now().plusDays(3);
        List<Loan> upcomingLoans = loanRepository.findByStatusAndDueDateBefore(LoanStatus.DISBURSED, targetDate);

        for (Loan loan : upcomingLoans) {
            log.info("======================================================");
            log.info("SIMULATED EMAIL SENT TO: {}", loan.getUser().getEmailAddress());
            log.info("SUBJECT: Repayment Reminder");
            log.info("BODY: Your loan {} is due on {}. Please ensure your wallet is funded.", loan.getId(), loan.getDueDate());
            log.info("======================================================");
        }
    }
}
