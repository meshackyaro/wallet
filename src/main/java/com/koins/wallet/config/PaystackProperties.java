package com.koins.wallet.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "paystack")
@Setter
@Getter
public class PaystackProperties {
    private Secret secret;

    @Getter
    @Setter
    public static class Secret {
        private String key;
    }
}
