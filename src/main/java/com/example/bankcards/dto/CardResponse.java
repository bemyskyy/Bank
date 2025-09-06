package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CardResponse {
    private Long id;
    private String maskedNumber;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;

    public static CardResponse from(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setMaskedNumber(maskCardNumber(card.getCardNumber()));
        response.setExpirationDate(card.getExpirationDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        return response;
    }

    private static String maskCardNumber(String number) {
        if (number == null || number.length() < 4) {
            return "****";
        }
        return "**** **** **** " + number.substring(number.length() - 4);
    }
}