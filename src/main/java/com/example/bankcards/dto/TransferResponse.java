package com.example.bankcards.dto;

import com.example.bankcards.entity.Transfer;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponse {
    private Long id;
    private String fromCardNumber;
    private String toCardNumber;
    private BigDecimal amount;
    private LocalDateTime createdAt;

    public static TransferResponse from(Transfer transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getFromCard().getCardNumber(),
                transfer.getToCard().getCardNumber(),
                transfer.getAmount(),
                transfer.getCreatedAt()
        );
    }
}