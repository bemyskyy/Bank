package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {
    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferRepository transferRepository;

    @InjectMocks
    private CardService cardService;

    @Test
    void createCard_ShouldSuccessfullyCreateCard() {
        // Arrange
        Long userId = 1L;
        User owner = new User();
        owner.setId(userId);
        owner.setUsername("testuser");

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(1L);
            return card;
        });

        // Act
        Card result = cardService.createCard(userId);

        // Assert
        assertNotNull(result);
        assertEquals(owner, result.getOwner());
        assertNotNull(result.getCardNumber());
        assertEquals(LocalDate.now().plusYears(3), result.getExpirationDate());
        assertEquals(CardStatus.ACTIVE, result.getStatus());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    void createCard_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cardService.createCard(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void deleteCard_ShouldSuccessfullyDeleteCard() {
        // Arrange
        Long cardId = 1L;
        when(cardRepository.existsById(cardId)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(cardId);

        // Act & Assert
        assertDoesNotThrow(() -> cardService.deleteCard(cardId));
        verify(cardRepository, times(1)).existsById(cardId);
        verify(cardRepository, times(1)).deleteById(cardId);
    }

    @Test
    void deleteCard_ShouldThrowExceptionWhenCardNotFound() {
        // Arrange
        Long cardId = 999L;
        when(cardRepository.existsById(cardId)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cardService.deleteCard(cardId));
        verify(cardRepository, times(1)).existsById(cardId);
        verify(cardRepository, never()).deleteById(anyLong());
    }

    @Test
    void transfer_ShouldSuccessfullyTransferBetweenOwnCards() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        User owner = new User();
        owner.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("200.00"));
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(owner);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(1L);
            return transfer;
        });

        // Act
        Transfer result = cardService.transfer(fromCardId, toCardId, amount);

        // Assert
        assertNotNull(result);
        assertEquals(fromCard, result.getFromCard());
        assertEquals(toCard, result.getToCard());
        assertEquals(amount, result.getAmount());
        assertEquals(new BigDecimal("100.00"), fromCard.getBalance());
        assertEquals(new BigDecimal("150.00"), toCard.getBalance());
        verify(cardRepository, times(2)).findById(anyLong());
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenFromCardNotFound() {
        // Arrange
        Long fromCardId = 999L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));
        verify(cardRepository, times(1)).findById(fromCardId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenToCardNotFound() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 999L;
        BigDecimal amount = new BigDecimal("100.00");

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));
        verify(cardRepository, times(1)).findById(fromCardId);
        verify(cardRepository, times(1)).findById(toCardId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenFromCardBlocked() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        User owner = new User();
        owner.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(owner);
        fromCard.setStatus(CardStatus.BLOCKED);

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(owner);
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));

        assertEquals("Отправляющая карта заблокирована", exception.getMessage());
        verify(cardRepository, times(2)).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenToCardBlocked() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        User owner = new User();
        owner.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(owner);
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(owner);
        toCard.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));

        assertEquals("Нельзя перевести деньги на заблокированную карту", exception.getMessage());
        verify(cardRepository, times(2)).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenDifferentOwners() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("100.00");

        User owner1 = new User();
        owner1.setId(1L);

        User owner2 = new User();
        owner2.setId(2L);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(owner1);
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(owner2);
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));

        assertEquals("Можно переводить только между своими картами", exception.getMessage());
        verify(cardRepository, times(2)).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void transfer_ShouldThrowExceptionWhenInsufficientFunds() {
        // Arrange
        Long fromCardId = 1L;
        Long toCardId = 2L;
        BigDecimal amount = new BigDecimal("300.00");

        User owner = new User();
        owner.setId(1L);

        Card fromCard = new Card();
        fromCard.setId(fromCardId);
        fromCard.setOwner(owner);
        fromCard.setBalance(new BigDecimal("200.00"));
        fromCard.setStatus(CardStatus.ACTIVE);

        Card toCard = new Card();
        toCard.setId(toCardId);
        toCard.setOwner(owner);
        toCard.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(fromCardId)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findById(toCardId)).thenReturn(Optional.of(toCard));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> cardService.transfer(fromCardId, toCardId, amount));

        assertEquals("Недостаточно средств", exception.getMessage());
        verify(cardRepository, times(2)).findById(anyLong());
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void blockCard_ShouldSuccessfullyBlockCard() {
        // Arrange
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.ACTIVE);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cardService.blockCard(cardId);

        // Assert
        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void blockCard_ShouldThrowExceptionWhenCardNotFound() {
        // Arrange
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cardService.blockCard(cardId));
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void activateCard_ShouldSuccessfullyActivateCard() {
        // Arrange
        Long cardId = 1L;
        Card card = new Card();
        card.setId(cardId);
        card.setStatus(CardStatus.BLOCKED);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        cardService.activateCard(cardId);

        // Assert
        assertEquals(CardStatus.ACTIVE, card.getStatus());
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void activateCard_ShouldThrowExceptionWhenCardNotFound() {
        // Arrange
        Long cardId = 999L;
        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> cardService.activateCard(cardId));
        verify(cardRepository, times(1)).findById(cardId);
        verify(cardRepository, never()).save(any(Card.class));
    }

    @Test
    void getUserCards_ShouldReturnUserCardsForAdmin() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        User owner = new User();
        owner.setId(userId);

        Card card1 = new Card();
        card1.setId(1L);
        card1.setOwner(owner);

        Card card2 = new Card();
        card2.setId(2L);
        card2.setOwner(owner);

        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(userRepository.findById(userId)).thenReturn(Optional.of(owner));
        when(cardRepository.findAllByOwner(owner, pageable)).thenReturn(cardPage);

        // Act
        Page<Card> result = cardService.getUserCards(userId, pageable, true, "admin");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userRepository, times(1)).findById(userId);
        verify(cardRepository, times(1)).findAllByOwner(owner, pageable);
    }

    @Test
    void getUserCards_ShouldReturnOwnCardsForUser() {
        // Arrange
        Long userId = 1L;
        Pageable pageable = Pageable.unpaged();
        User currentUser = new User();
        currentUser.setId(userId);
        currentUser.setUsername("testuser");

        Card card1 = new Card();
        card1.setId(1L);
        card1.setOwner(currentUser);

        Card card2 = new Card();
        card2.setId(2L);
        card2.setOwner(currentUser);

        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));
        when(cardRepository.findAllByOwner(currentUser, pageable)).thenReturn(cardPage);

        // Act
        Page<Card> result = cardService.getUserCards(userId, pageable, false, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, times(1)).findAllByOwner(currentUser, pageable);
    }

    @Test
    void getUserCards_ShouldThrowExceptionWhenUserAccessesOtherUsersCards() {
        // Arrange
        Long userId = 2L; // Different from current user
        Pageable pageable = Pageable.unpaged();
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class,
                () -> cardService.getUserCards(userId, pageable, false, "testuser"));

        assertEquals("Вы не можете просматривать карты другого пользователя", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(cardRepository, never()).findAllByOwner(any(), any());
    }

    @Test
    void getAllCards_ShouldReturnAllCards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        Card card1 = new Card();
        card1.setId(1L);

        Card card2 = new Card();
        card2.setId(2L);

        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(cardRepository.findAll(pageable)).thenReturn(cardPage);

        // Act
        Page<Card> result = cardService.getAllCards(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(cardRepository, times(1)).findAll(pageable);
    }

    @Test
    void getCardsByStatus_ShouldReturnFilteredCards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        CardStatus status = CardStatus.ACTIVE;

        Card card1 = new Card();
        card1.setId(1L);
        card1.setStatus(CardStatus.ACTIVE);

        Card card2 = new Card();
        card2.setId(2L);
        card2.setStatus(CardStatus.ACTIVE);

        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(cardRepository.findAllByStatus(status, pageable)).thenReturn(cardPage);

        // Act
        Page<Card> result = cardService.getCardsByStatus(status, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(cardRepository, times(1)).findAllByStatus(status, pageable);
    }

    @Test
    void getCardsExpiringBefore_ShouldReturnFilteredCards() {
        // Arrange
        Pageable pageable = Pageable.unpaged();
        LocalDate date = LocalDate.now().plusMonths(1);

        Card card1 = new Card();
        card1.setId(1L);
        card1.setExpirationDate(LocalDate.now().plusDays(10));

        Card card2 = new Card();
        card2.setId(2L);
        card2.setExpirationDate(LocalDate.now().plusDays(20));

        Page<Card> cardPage = new PageImpl<>(List.of(card1, card2));

        when(cardRepository.findAllByExpirationDateBefore(date, pageable)).thenReturn(cardPage);

        // Act
        Page<Card> result = cardService.getCardsExpiringBefore(date, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        verify(cardRepository, times(1)).findAllByExpirationDateBefore(date, pageable);
    }
}
