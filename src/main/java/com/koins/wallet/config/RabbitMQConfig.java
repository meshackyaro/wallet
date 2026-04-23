package com.koins.wallet.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String LOAN_EXCHANGE = "loan.exchange";
    public static final String LOAN_APPROVAL_QUEUE = "loan.approval.queue";
    public static final String LOAN_APPROVAL_ROUTING_KEY = "loan.approval.key";

    public static final String LOAN_DISBURSEMENT_QUEUE = "loan.disbursement.queue";
    public static final String LOAN_DISBURSEMENT_ROUTING_KEY = "loan.disbursement.key";

    public static final String LOAN_REPAYMENT_QUEUE = "loan.repayment.queue";
    public static final String LOAN_REPAYMENT_ROUTING_KEY = "loan.repayment.key";

    @Bean
    public Queue loanApprovalQueue() {
        return new Queue(LOAN_APPROVAL_QUEUE);
    }

    @Bean
    public Queue loanDisbursementQueue() {
        return new Queue(LOAN_DISBURSEMENT_QUEUE);
    }

    @Bean
    public Queue loanRepaymentQueue() {
        return new Queue(LOAN_REPAYMENT_QUEUE);
    }

    @Bean
    public TopicExchange loanExchange() {
        return new TopicExchange(LOAN_EXCHANGE);
    }

    @Bean
    public Binding approvalBinding(Queue loanApprovalQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(loanApprovalQueue).to(loanExchange).with(LOAN_APPROVAL_ROUTING_KEY);
    }

    @Bean
    public Binding disbursementBinding(Queue loanDisbursementQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(loanDisbursementQueue).to(loanExchange).with(LOAN_DISBURSEMENT_ROUTING_KEY);
    }

    @Bean
    public Binding repaymentBinding(Queue loanRepaymentQueue, TopicExchange loanExchange) {
        return BindingBuilder.bind(loanRepaymentQueue).to(loanExchange).with(LOAN_REPAYMENT_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }
}

