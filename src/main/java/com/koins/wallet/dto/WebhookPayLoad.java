package com.koins.wallet.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WebhookPayLoad {
    private String event;
    private WebhookData data;

    @Data
    public static class WebhookData {
        private String reference;
        private BigDecimal amount;
        private String status;
        private WebhookCustomer customer;
    }

    @Data
    public static class WebhookCustomer {
        private String email;
    }
}
