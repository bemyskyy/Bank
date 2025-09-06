package com.example.bankcards.service;

import com.example.bankcards.entity.*;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Random;
import java.util.UUID;

@Service
public class CardService {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;

    public CardService(CardRepository cardRepository,
                       UserRepository userRepository,
                       TransferRepository transferRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
    }

    @Transactional
    public Card createCard(Long userId) {
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = new Card();
        card.setOwner(owner);
        card.setCardNumber(generateCardNumber());
        card.setExpirationDate(LocalDate.now().plusYears(3));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        return cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long id) {
        if (!cardRepository.existsById(id)) {
            throw new EntityNotFoundException("Card not found");
        }
        cardRepository.deleteById(id);
    }

    @Transactional
    public Transfer transfer(Long fromCardId, Long toCardId, BigDecimal amount) {
        Card from = cardRepository.findById(fromCardId)
                .orElseThrow(() -> new EntityNotFoundException("From card not found"));
        Card to = cardRepository.findById(toCardId)
                .orElseThrow(() -> new EntityNotFoundException("To card not found"));

        if (from.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Отправляющая карта заблокирована");
        }
        if (to.getStatus() == CardStatus.BLOCKED) {
            throw new IllegalStateException("Нельзя перевести деньги на заблокированную карту");
        }

        if (!from.getOwner().equals(to.getOwner())) {
            throw new IllegalArgumentException("Можно переводить только между своими картами");
        }

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно средств");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        Transfer transfer = new Transfer();
        transfer.setFromCard(from);
        transfer.setToCard(to);
        transfer.setAmount(amount);

        cardRepository.save(from);
        cardRepository.save(to);

        return transferRepository.save(transfer);
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));
        card.setStatus(CardStatus.BLOCKED);
    }

    @Transactional
    public void activateCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));
        card.setStatus(CardStatus.ACTIVE);
    }

    public Page<Card> getUserCards(Long userId, Pageable pageable, boolean isAdmin, String currentUsername) {
        if (!isAdmin) {
            User currentUser = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            if (!currentUser.getId().equals(userId)) {
                throw new SecurityException("Вы не можете просматривать карты другого пользователя");
            }

            return cardRepository.findAllByOwner(currentUser, pageable);
        } else {
            User owner = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
            return cardRepository.findAllByOwner(owner, pageable);
        }
    }

    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    public Page<Card> getCardsByStatus(CardStatus status, Pageable pageable) {
        return cardRepository.findAllByStatus(status, pageable);
    }

    public Page<Card> getCardsExpiringBefore(LocalDate date, Pageable pageable) {
        return cardRepository.findAllByExpirationDateBefore(date, pageable);
    }

    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();
        Random random = new Random();

        for (int i = 0; i < 15; i++) {
            cardNumber.append(random.nextInt(10));
        }

        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }
}