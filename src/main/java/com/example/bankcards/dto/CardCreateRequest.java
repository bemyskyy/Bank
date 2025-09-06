package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardCreateRequest {
    @NotNull
    private Long ownerId;
}
