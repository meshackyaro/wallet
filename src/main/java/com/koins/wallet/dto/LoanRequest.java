package com.koins.wallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanRequest {
    private BigDecimal amount;

    @NotNull(message = "Duration in days is required")
    @Min(value = 7, message = "Minimum loan duration is 7 days")
    private Integer durationDays;
}
