package com.example.bankcards.service;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlockRequestService {
    private final BlockRequestRepository blockRequestRepository;
    private final CardRepository cardRepository;

    public BlockRequestService(BlockRequestRepository blockRequestRepository, CardRepository cardRepository) {
        this.blockRequestRepository = blockRequestRepository;
        this.cardRepository = cardRepository;
    }

    @Transactional
    public BlockRequest createRequest(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));

        if (card.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        BlockRequest request = new BlockRequest();
        request.setCard(card);
        request.setStatus(RequestStatus.PENDING);

        return blockRequestRepository.save(request);
    }

    @Transactional
    public BlockRequest approveRequest(Long requestId) {
        BlockRequest request = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Заявка уже обработана");
        }

        request.setStatus(RequestStatus.APPROVED);
        Card card = request.getCard();
        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

        return blockRequestRepository.save(request);
    }

    @Transactional
    public BlockRequest rejectRequest(Long requestId) {
        BlockRequest request = blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("Заявка не найдена"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new IllegalStateException("Заявка уже обработана");
        }

        request.setStatus(RequestStatus.REJECTED);
        return blockRequestRepository.save(request);
    }

    public List<BlockRequest> getPendingRequests() {
        return blockRequestRepository.findAllByStatus(RequestStatus.PENDING);
    }
}