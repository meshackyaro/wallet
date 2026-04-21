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
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal amount;

    private Integer interestRate;

    private Integer loanDuration;

    @Enumerated(EnumType.STRING)
    private LoanStatus loanStatus;

    @Column(columnDefinition = "TEXT")
    private String repaymentSchedule;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
