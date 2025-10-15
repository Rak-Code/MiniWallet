package com.example.miniwallet.service;

import com.example.miniwallet.entity.Transaction;
import com.example.miniwallet.exception.ResourceNotFoundException;
import com.example.miniwallet.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;

    @Cacheable(value = "transactions", key = "'transaction_' + #id")
    public Transaction getById(String id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));
    }

    @Cacheable(value = "transactions", key = "'transactions_wallet_' + #walletId + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Transaction> getByWalletId(Long walletId, Pageable pageable) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    public Page<Transaction> getByWalletIdAndStatus(Long walletId, Transaction.TransactionStatus status, Pageable pageable) {
        return transactionRepository.findByWalletIdAndStatusOrderByCreatedAtDesc(walletId, status, pageable);
    }

    public Page<Transaction> getByWalletIdAndDateRange(Long walletId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return transactionRepository.findByWalletIdAndDateRange(walletId, start, end, pageable);
    }

    public Page<Transaction> getByWalletIdAndType(Long walletId, Transaction.TransactionType type, Pageable pageable) {
        return transactionRepository.findByWalletIdAndType(walletId, type, pageable);
    }

    public List<Transaction> findByReference(String reference) {
        return transactionRepository.findByReference(reference);
    }

    public List<Transaction> findByTransferId(String transferId) {
        return transactionRepository.findByTransferId(transferId);
    }

    public Page<Transaction> getAllTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }
}
