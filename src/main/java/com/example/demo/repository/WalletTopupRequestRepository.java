package com.example.demo.repository;

import com.example.demo.entity.WalletTopupRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletTopupRequestRepository extends JpaRepository<WalletTopupRequest, Long> {
    List<WalletTopupRequest> findByStatusOrderByCreatedAtDesc(String status);
    org.springframework.data.domain.Page<WalletTopupRequest> findByStatusNotOrderByCreatedAtDesc(String status, Pageable pageable);
    List<WalletTopupRequest> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Query("select r.user, sum(r.amount) as total from WalletTopupRequest r " +
           "where r.status = 'APPROVED' " +
           "group by r.user " +
           "order by total desc")
    List<Object[]> findTopToppedUpUsers(Pageable pageable);
}
