package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {
    List<BlockRequest> findAllByStatus(RequestStatus status);
}