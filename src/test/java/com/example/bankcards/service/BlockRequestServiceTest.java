package com.example.bankcards.service;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.RequestStatus;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockRequestServiceTest {
    @Mock
    private BlockRequestRepository blockRequestRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private BlockRequestService blockRequestService;

    @Test
    void createRequest_ShouldSuccessfullyCreateRequest() {
        // Arrange
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(blockRequestRepository.save(any(BlockRequest.class))).thenAnswer(invocation -> {
            BlockRequest request = invocation.getArgument(0);
            request.setId(1L);
            return request;
        });

        // Act
        BlockRequest result = blockRequestService.createRequest(cardId);

        // Assert
        assertNotNull(result);
        assertEquals(card, result.getCard());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        verify(cardRepository, times(1)).findById(cardId);
        verify(blockRequestRepository, times(1)).save(any(BlockRequest.class));
    }

    @Test
    void createRequest_ShouldThrowExceptionWhenCardNotFound() {
        // Arrange
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blockRequestService.createRequest(cardId));
        verify(cardRepository, times(1)).findById(cardId);
        verify(blockRequestRepository, never()).save(any(BlockRequest.class));
    }

    @Test
    void createRequest_ShouldThrowExceptionWhenCardAlreadyBlocked() {
        // Arrange
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> blockRequestService.createRequest(cardId));

        assertEquals("Карта уже заблокирована", exception.getMessage());
        verify(cardRepository, times(1)).findById(cardId);
        verify(blockRequestRepository, never()).save(any(BlockRequest.class));
    }

    @Test
    void approveRequest_ShouldSuccessfullyApproveRequest() {
        // Arrange
        Long requestId = 1L;
        Card card = new Card();
        card.setId(1L);
        card.setStatus(CardStatus.ACTIVE);

        BlockRequest request = new BlockRequest();
        request.setId(requestId);
        request.setCard(card);
        request.setStatus(RequestStatus.PENDING);

        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(blockRequestRepository.save(any(BlockRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BlockRequest result = blockRequestService.approveRequest(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.APPROVED, result.getStatus());
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(cardRepository, times(1)).save(card);
        verify(blockRequestRepository, times(1)).save(request);
    }

    @Test
    void approveRequest_ShouldThrowExceptionWhenRequestNotFound() {
        // Arrange
        Long requestId = 999L;
        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blockRequestService.approveRequest(requestId));
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void approveRequest_ShouldThrowExceptionWhenRequestAlreadyProcessed() {
        // Arrange
        Long requestId = 1L;
        BlockRequest request = new BlockRequest();
        request.setId(requestId);
        request.setStatus(RequestStatus.APPROVED);

        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> blockRequestService.approveRequest(requestId));

        assertEquals("Заявка уже обработана", exception.getMessage());
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void rejectRequest_ShouldSuccessfullyRejectRequest() {
        // Arrange
        Long requestId = 1L;
        BlockRequest request = new BlockRequest();
        request.setId(requestId);
        request.setStatus(RequestStatus.PENDING);

        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(blockRequestRepository.save(any(BlockRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        BlockRequest result = blockRequestService.rejectRequest(requestId);

        // Assert
        assertNotNull(result);
        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(blockRequestRepository, times(1)).save(request);
    }

    @Test
    void rejectRequest_ShouldThrowExceptionWhenRequestNotFound() {
        // Arrange
        Long requestId = 999L;
        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> blockRequestService.rejectRequest(requestId));
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(blockRequestRepository, never()).save(any(BlockRequest.class));
    }

    @Test
    void rejectRequest_ShouldThrowExceptionWhenRequestAlreadyProcessed() {
        // Arrange
        Long requestId = 1L;
        BlockRequest request = new BlockRequest();
        request.setId(requestId);
        request.setStatus(RequestStatus.APPROVED);

        when(blockRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> blockRequestService.rejectRequest(requestId));

        assertEquals("Заявка уже обработана", exception.getMessage());
        verify(blockRequestRepository, times(1)).findById(requestId);
        verify(blockRequestRepository, never()).save(any(BlockRequest.class));
    }

    @Test
    void getPendingRequests_ShouldReturnPendingRequests() {
        // Arrange
        BlockRequest request1 = new BlockRequest();
        request1.setId(1L);
        request1.setStatus(RequestStatus.PENDING);

        BlockRequest request2 = new BlockRequest();
        request2.setId(2L);
        request2.setStatus(RequestStatus.PENDING);

        when(blockRequestRepository.findAllByStatus(RequestStatus.PENDING)).thenReturn(List.of(request1, request2));

        // Act
        List<BlockRequest> result = blockRequestService.getPendingRequests();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(blockRequestRepository, times(1)).findAllByStatus(RequestStatus.PENDING);
    }
}