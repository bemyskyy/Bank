package com.example.bankcards.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    @NotNull(message = "ID карты отправителя обязателен")
    @Min(value = 1, message = "ID карты отправителя должен быть положительным")
    private Long fromCardId;

    @NotNull(message = "ID карты получателя обязателен")
    @Min(value = 1, message = "ID карты получателя должен быть положительным")
    private Long toCardId;

    @NotNull(message = "Сумма перевода обязательна")
    @Positive(message = "Сумма перевода должна быть положительной")
    @Digits(integer = 12, fraction = 2, message = "Сумма перевода недопустимого формата")
    private BigDecimal amount;
}