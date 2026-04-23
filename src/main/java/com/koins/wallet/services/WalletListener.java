package com.koins.wallet.services;

import com.koins.wallet.entities.User;
import com.koins.wallet.entities.Wallet;
import com.koins.wallet.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletListener {

    private final WalletRepository walletRepository;

    @EventListener
    public void handleUserRegistration(User user) {
        log.info("Creating wallet for user: {}", user.getEmailAddress());
        Wallet wallet = Wallet.builder()
                .user(user)
                .build();
        walletRepository.save(wallet);
        log.info("Wallet created successfully for user: {}", user.getEmailAddress());
    }
}
