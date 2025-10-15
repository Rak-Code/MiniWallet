package com.example.miniwallet.repository;

import com.example.miniwallet.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    
    Page<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    List<Transaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    
    List<Transaction> findByTransferId(String transferId);
    
    Page<Transaction> findByWalletIdAndStatusOrderByCreatedAtDesc(
        Long walletId,
        Transaction.TransactionStatus status,
        Pageable pageable
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletIdAndDateRange(
        @Param("walletId") Long walletId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        Pageable pageable
    );
    
    @Query("SELECT t FROM Transaction t WHERE t.wallet.id = :walletId " +
           "AND t.type = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByWalletIdAndType(
        @Param("walletId") Long walletId,
        @Param("type") Transaction.TransactionType type,
        Pageable pageable
    );
    
    List<Transaction> findByReference(String reference);
}
