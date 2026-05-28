package com.example.demo.repository;

import com.example.demo.entity.AccountGame;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountGame, Long>, JpaSpecificationExecutor<AccountGame>{
    List<AccountGame> findByCategoryIdAndStatus(Long categoryId, Integer status);
    Page<AccountGame> findByCategoryIdAndStatus(Long categoryId, Integer status, Pageable pageable);
    List<AccountGame> findByStatus(Integer status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccountGame a where a.id = :id")
    Optional<AccountGame> findByIdForUpdate(@Param("id") Long id);
}
