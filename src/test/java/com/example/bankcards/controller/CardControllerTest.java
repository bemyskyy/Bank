package com.example.bankcards.controller;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCard_ShouldReturnCardResponse() throws Exception {
        Card card = new Card();
        card.setId(1L);
        card.setCardNumber("1234567890123456");
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        Mockito.when(cardService.createCard(eq(1L)))
                .thenReturn(card);

        mockMvc.perform(post("/api/cards/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getUserCards_ShouldReturnPageOfCards() throws Exception {
        Card card = new Card();
        card.setId(2L);
        card.setCardNumber("9999999999999999");
        card.setExpirationDate(LocalDate.now().plusYears(2));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.TEN);

        Mockito.when(cardService.getUserCards(eq(2L), any(Pageable.class), anyBoolean(), anyString()))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards/user/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllCards_ShouldReturnPageOfCards() throws Exception {
        Card card = new Card();
        card.setId(3L);

        Mockito.when(cardService.getAllCards(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(3L));
    }

    @Test
    @WithMockUser(roles = "USER")
    void transfer_ShouldReturnTransferResponse() throws Exception {
        Card fromCard = new Card(1L, "1111222233334444", BigDecimal.valueOf(1000), CardStatus.ACTIVE);
        Card toCard = new Card(2L, "5555666677778888", BigDecimal.valueOf(500), CardStatus.ACTIVE);

        Transfer transfer = new Transfer();
        transfer.setId(10L);
        transfer.setFromCard(fromCard);
        transfer.setToCard(toCard);
        transfer.setAmount(BigDecimal.valueOf(100));
        transfer.setCreatedAt(LocalDateTime.now());

        Mockito.when(cardService.transfer(anyLong(), anyLong(), any(BigDecimal.class)))
                .thenReturn(transfer);

        String requestJson = """
        {"fromCardId":1,"toCardId":2,"amount":100}
        """;

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.fromCardNumber").value("1111222233334444"))
                .andExpect(jsonPath("$.toCardNumber").value("5555666677778888"))
                .andExpect(jsonPath("$.amount").value(100));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void blockCard_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/cards/{id}/block", 5L))
                .andExpect(status().isNoContent());

        Mockito.verify(cardService).blockCard(5L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void activateCard_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(put("/api/cards/{id}/activate", 6L))
                .andExpect(status().isNoContent());

        Mockito.verify(cardService).activateCard(6L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCard_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/cards/{id}", 7L))
                .andExpect(status().isNoContent());

        Mockito.verify(cardService).deleteCard(7L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByStatus_ShouldReturnCards() throws Exception {
        Card card = new Card();
        card.setId(8L);
        card.setStatus(CardStatus.ACTIVE);

        Mockito.when(cardService.getCardsByStatus(eq(CardStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards/status/ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(8L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getByExpirationDate_ShouldReturnCards() throws Exception {
        Card card = new Card();
        card.setId(9L);
        card.setExpirationDate(LocalDate.of(2030, 1, 1));

        Mockito.when(cardService.getCardsExpiringBefore(eq(LocalDate.of(2030, 1, 1)), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(card)));

        mockMvc.perform(get("/api/cards/expiring-before/2030-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(9L));
    }
}