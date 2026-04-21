package com.koins.wallet.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    private String currency;

    @Enumerated(EnumType.STRING)
    private WalletStatus walletStatus;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
