package com.koins.wallet.services;

import com.koins.wallet.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void publishLoanApprovalEvent(String email, String loanId) {
        log.info("Publishing loan approval event for user: {}, loan: {}", email, loanId);
        Map<String, String> message = Map.of(
                "email", email,
                "loanId", loanId
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_EXCHANGE, RabbitMQConfig.LOAN_APPROVAL_ROUTING_KEY, message);
    }

    public void publishLoanDisbursementEvent(String email, String loanId, String amount) {
        log.info("Publishing loan disbursement event for user: {}, loan: {}", email, loanId);
        Map<String, String> message = Map.of(
                "email", email,
                "loanId", loanId,
                "amount", amount
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_EXCHANGE, RabbitMQConfig.LOAN_DISBURSEMENT_ROUTING_KEY, message);
    }

    public void publishLoanRepaymentEvent(String email, String loanId, String amount) {
        log.info("Publishing loan repayment event for user: {}, loan: {}", email, loanId);
        Map<String, String> message = Map.of(
                "email", email,
                "loanId", loanId,
                "amount", amount
        );
        rabbitTemplate.convertAndSend(RabbitMQConfig.LOAN_EXCHANGE, RabbitMQConfig.LOAN_REPAYMENT_ROUTING_KEY, message);
    }
}
