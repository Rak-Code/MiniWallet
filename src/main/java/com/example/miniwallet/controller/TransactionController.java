package com.example.miniwallet.controller;

import com.example.miniwallet.dto.TransactionResponseDto;
import com.example.miniwallet.entity.Transaction;
import com.example.miniwallet.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getById(@PathVariable String id) {
        Transaction t = transactionService.getById(id);
        return ResponseEntity.ok(toDto(t));
    }

    @GetMapping("/by-wallet/{walletId}")
    public ResponseEntity<Page<TransactionResponseDto>> listByWallet(
            @PathVariable Long walletId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Transaction.TransactionStatus status,
            @RequestParam(required = false) Transaction.TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> pageResult;

        if (status != null) {
            pageResult = transactionService.getByWalletIdAndStatus(walletId, status, pageable);
        } else if (type != null) {
            pageResult = transactionService.getByWalletIdAndType(walletId, type, pageable);
        } else if (start != null && end != null) {
            pageResult = transactionService.getByWalletIdAndDateRange(walletId, start, end, pageable);
        } else {
            pageResult = transactionService.getByWalletId(walletId, pageable);
        }

        Page<TransactionResponseDto> dtoPage = pageResult.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/by-reference/{reference}")
    public ResponseEntity<java.util.List<TransactionResponseDto>> getByReference(@PathVariable String reference) {
        java.util.List<Transaction> transactions = transactionService.findByReference(reference);
        java.util.List<TransactionResponseDto> dtoList = transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/by-transfer/{transferId}")
    public ResponseEntity<java.util.List<TransactionResponseDto>> getByTransferId(@PathVariable String transferId) {
        java.util.List<Transaction> transactions = transactionService.findByTransferId(transferId);
        java.util.List<TransactionResponseDto> dtoList = transactions.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping
    public ResponseEntity<Page<TransactionResponseDto>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> pageResult = transactionService.getAllTransactions(pageable);
        Page<TransactionResponseDto> dtoPage = pageResult.map(this::toDto);
        return ResponseEntity.ok(dtoPage);
    }

    private TransactionResponseDto toDto(Transaction t) {
        return TransactionResponseDto.builder()
                .id(t.getTransactionId())
                .walletId(t.getWallet().getWalletId())
                .amount(t.getAmount())
                .type(t.getType().name())
                .status(t.getStatus().name())
                .transferId(t.getTransferId())
                .relatedWalletId(t.getRelatedWallet() != null ? t.getRelatedWallet().getWalletId() : null)
                .reference(t.getReference())
                .createdAt(t.getCreatedAt())
                .build();
    }
}
