package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Page<Card> findAllByOwner(User owner, Pageable pageable);
    Page<Card> findAllByStatus(CardStatus status, Pageable pageable);
    Page<Card> findAllByExpirationDateBefore(LocalDate date, Pageable pageable);
}