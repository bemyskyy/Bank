package com.example.bankcards.dto;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlockRequestResponse {
    private Long id;
    private Long cardId;
    private RequestStatus status;

    public static BlockRequestResponse from(BlockRequest request) {
        BlockRequestResponse dto = new BlockRequestResponse();
        dto.setId(request.getId());
        dto.setCardId(request.getCard().getId());
        dto.setStatus(request.getStatus());
        return dto;
    }
}