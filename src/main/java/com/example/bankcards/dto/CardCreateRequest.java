package com.example.bankcards.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardCreateRequest {
    @NotNull(message = "ID владельца обязателен")
    @Min(value = 1, message = "ID владельца должен быть положительным")
    private Long ownerId;
}