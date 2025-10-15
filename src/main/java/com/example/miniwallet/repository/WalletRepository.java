package com.example.miniwallet.repository;

import com.example.miniwallet.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    
    @Query("SELECT w FROM Wallet w WHERE w.customer.customerId = :customerId")
    Optional<Wallet> findByCustomerId(@Param("customerId") Long customerId);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdWithLock(@Param("id") Long id);
    
    @Query("SELECT COUNT(w) > 0 FROM Wallet w WHERE w.customer.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);
}
