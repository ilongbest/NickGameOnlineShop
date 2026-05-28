package com.example.demo.repository;

import com.example.demo.entity.WalletTopupRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTopupRequestRepository extends JpaRepository<WalletTopupRequest, Long> {
    List<WalletTopupRequest> findByStatusOrderByCreatedAtDesc(String status);
    List<WalletTopupRequest> findByUserIdOrderByCreatedAtDesc(Long userId);
}
