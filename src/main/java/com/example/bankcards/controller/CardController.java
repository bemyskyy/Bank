package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateRequest;
import com.example.bankcards.dto.CardResponse;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.dto.TransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/cards")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        Card card = cardService.createCard(request.getOwnerId());
        return ResponseEntity.ok(CardResponse.from(card));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getUserCards(
            @PathVariable Long userId,
            Pageable pageable,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Page<Card> cards = cardService.getUserCards(userId, pageable, isAdmin, currentUser.getUsername());
        return ResponseEntity.ok(cards.map(CardResponse::from));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(pageable).map(CardResponse::from));
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        Transfer transfer = cardService.transfer(
                request.getFromCardId(),
                request.getToCardId(),
                request.getAmount()
        );
        return ResponseEntity.ok(TransferResponse.from(transfer));
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> blockCard(@PathVariable Long id) {
        cardService.blockCard(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable Long id) {
        cardService.activateCard(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getByStatus(@PathVariable CardStatus status, Pageable pageable) {
        return ResponseEntity.ok(cardService.getCardsByStatus(status, pageable).map(CardResponse::from));
    }

    @GetMapping("/expiring-before/{date}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getByExpirationDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getCardsExpiringBefore(date, pageable).map(CardResponse::from));
    }
}