package com.koins.wallet.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.koins.wallet.config.PaystackProperties;
import com.koins.wallet.dto.WebhookPayload;
import com.koins.wallet.services.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WalletService walletService;
    private final PaystackProperties paystackProperties;
    private final ObjectMapper objectMapper;

    @PostMapping("/paystack")
    public ResponseEntity<String> handlePaystackWebhook(
            @RequestBody String rawBody,
            @RequestHeader("x-paystack-signature") String signature
    ) {
        if (!verifySignature(rawBody, signature)) {
            log.error("Invalid webhook signature received!");
            return ResponseEntity.status(401).body("Invalid signature");
        }

        try {
            WebhookPayload payload = objectMapper.readValue(rawBody, WebhookPayload.class);
            log.info("Received verified webhook event: {}", payload.getEvent());

            if ("charge.success".equals(payload.getEvent()) && "success".equals(payload.getData().getStatus())) {
                String email = payload.getData().getCustomer().getEmail();
                BigDecimal amount = payload.getData().getAmount();
                String reference = payload.getData().getReference();

                walletService.fundWallet(email, amount, reference);
                log.info("Wallet funded for {}, amount: {}", email, amount);
            }
        } catch (Exception e) {
            log.error("Failed to process webhook: {}", e.getMessage());
        }

        return ResponseEntity.ok("Received");
    }

    private boolean verifySignature(String rawBody, String signature) {
        try {
            String secret = paystackProperties.getSecret().getKey();
            Mac sha512Hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            sha512Hmac.init(secretKey);

            byte[] hashBytes = sha512Hmac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(signature);
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage());
            return false;
        }
    }
}
