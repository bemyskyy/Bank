package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    @NotNull
    private Long fromCardId;

    @NotNull
    private Long toCardId;

    @NotNull
    @Positive
    private BigDecimal amount;
}
