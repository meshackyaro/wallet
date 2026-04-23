package com.koins.wallet.services;

import com.koins.wallet.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class RabbitMQListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.LOAN_APPROVAL_QUEUE)
    public void consumeLoanApproval(Map<String, String> message) {
        String email = message.get("email");
        String loanId = message.get("loanId");

        String subject = "Loan Approved";
        String body = String.format("Your loan application with ID %s has been approved and is ready for disbursement.", loanId);

        emailService.sendEmail(email, subject, body);
    }

    @RabbitListener(queues = RabbitMQConfig.LOAN_DISBURSEMENT_QUEUE)
    public void consumeLoanDisbursement(Map<String, String> message) {
        String email = message.get("email");
        String loanId = message.get("loanId");
        String amount = message.get("amount");

        String subject = "Loan Disbursed";
        String body = String.format("Your loan with ID %s of amount %s has been successfully disbursed to your wallet.", loanId, amount);

        emailService.sendEmail(email, subject, body);
    }

    @RabbitListener(queues = RabbitMQConfig.LOAN_REPAYMENT_QUEUE)
    public void consumeLoanRepayment(Map<String, String> message) {
        String email = message.get("email");
        String loanId = message.get("loanId");
        String amount = message.get("amount");

        String subject = "Loan Repayment Successful";
        String body = String.format("Your repayment for loan ID %s of amount %s was successful. Thank you for using Koins.", loanId, amount);

        emailService.sendEmail(email, subject, body);
    }
}
