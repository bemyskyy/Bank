package com.example.bankcards.controller;

import com.example.bankcards.dto.BlockRequestResponse;
import com.example.bankcards.service.BlockRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/block-requests")
public class BlockRequestController {
    private final BlockRequestService blockRequestService;

    public BlockRequestController(BlockRequestService blockRequestService) {
        this.blockRequestService = blockRequestService;
    }

    // пользователь создает заявку
    @PostMapping("/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BlockRequestResponse> requestBlock(@PathVariable Long cardId) {
        return ResponseEntity.ok(BlockRequestResponse.from(blockRequestService.createRequest(cardId)));
    }

    // админ подтверждает заявку
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlockRequestResponse> approve(@PathVariable Long id) {
        return ResponseEntity.ok(BlockRequestResponse.from(blockRequestService.approveRequest(id)));
    }

    // админ отклоняет заявку
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BlockRequestResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(BlockRequestResponse.from(blockRequestService.rejectRequest(id)));
    }

    // админ смотрит все заявки
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BlockRequestResponse>> getPending() {
        return ResponseEntity.ok(
                blockRequestService.getPendingRequests()
                        .stream()
                        .map(BlockRequestResponse::from)
                        .toList()
        );
    }
}
