package com.example.bankcards.controller;

import com.example.bankcards.entity.*;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.BlockRequestService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BlockRequestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BlockRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlockRequestService blockRequestService;

    @MockitoBean
    private JwtService jwtService;

    private BlockRequest buildRequest(Long id, Long cardId, RequestStatus status) {
        Card card = new Card();
        card.setId(cardId);

        BlockRequest request = new BlockRequest();
        request.setId(id);
        request.setCard(card);
        request.setStatus(status);
        request.setCreatedAt(LocalDateTime.now());
        return request;
    }

    @Test
    @WithMockUser(roles = "USER")
    void requestBlock_ShouldReturnPendingRequest() throws Exception {
        BlockRequest request = buildRequest(1L, 10L, RequestStatus.PENDING);
        Mockito.when(blockRequestService.createRequest(eq(10L))).thenReturn(request);

        mockMvc.perform(post("/api/block-requests/{cardId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.cardId").value(10L))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approve_ShouldReturnApprovedRequest() throws Exception {
        BlockRequest request = buildRequest(2L, 20L, RequestStatus.APPROVED);
        Mockito.when(blockRequestService.approveRequest(eq(2L))).thenReturn(request);

        mockMvc.perform(put("/api/block-requests/{id}/approve", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.cardId").value(20L))
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reject_ShouldReturnRejectedRequest() throws Exception {
        BlockRequest request = buildRequest(3L, 30L, RequestStatus.REJECTED);
        Mockito.when(blockRequestService.rejectRequest(eq(3L))).thenReturn(request);

        mockMvc.perform(put("/api/block-requests/{id}/reject", 3L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3L))
                .andExpect(jsonPath("$.cardId").value(30L))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPending_ShouldReturnListOfPendingRequests() throws Exception {
        BlockRequest req1 = buildRequest(4L, 40L, RequestStatus.PENDING);
        BlockRequest req2 = buildRequest(5L, 50L, RequestStatus.PENDING);

        Mockito.when(blockRequestService.getPendingRequests()).thenReturn(List.of(req1, req2));

        mockMvc.perform(get("/api/block-requests/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(4L))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].id").value(5L))
                .andExpect(jsonPath("$[1].status").value("PENDING"));
    }
}