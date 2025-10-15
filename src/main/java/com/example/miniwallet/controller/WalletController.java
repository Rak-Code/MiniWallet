package com.example.miniwallet.controller;

import com.example.miniwallet.dto.*;
import com.example.miniwallet.entity.Transaction;
import com.example.miniwallet.entity.Wallet;
import com.example.miniwallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Validated
public class WalletController {

    private final WalletService walletService;

    // create wallet for customer
    @PostMapping
    public ResponseEntity<WalletResponseDto> createForCustomer(@RequestParam Long customerId) {
        Wallet w = walletService.createWalletForCustomer(customerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(w));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WalletResponseDto> getById(@PathVariable Long id) {
        Wallet w = walletService.getById(id);
        return ResponseEntity.ok(toDto(w));
    }

    @GetMapping("/by-customer/{customerId}")
    public ResponseEntity<WalletResponseDto> getByCustomer(@PathVariable Long customerId) {
        return walletService.findByCustomerId(customerId)
                .map(w -> ResponseEntity.ok(toDto(w)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<TransactionResponseDto> deposit(@PathVariable Long id,
                                                          @RequestBody @Validated DepositWithdrawRequestDto req) {
        Transaction tx = walletService.deposit(id, req.getAmount(), req.getReference());
        return ResponseEntity.status(HttpStatus.CREATED).body(toTxDto(tx));
    }

    @PostMapping("/{id}/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(@PathVariable Long id,
                                                           @RequestBody @Validated DepositWithdrawRequestDto req) {
        Transaction tx = walletService.withdraw(id, req.getAmount(), req.getReference());
        return ResponseEntity.ok(toTxDto(tx));
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponseDto>> transfer(@RequestBody @Validated TransferRequestDto req) {
        List<Transaction> txs = walletService.transfer(
                req.getFromWalletId(),
                req.getToWalletId(),
                req.getAmount(),
                req.getTransferId(),
                req.getReference()
        );
        List<TransactionResponseDto> dtos = txs.stream().map(this::toTxDto).collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.CREATED).body(dtos);
    }

    private WalletResponseDto toDto(Wallet w) {
        return WalletResponseDto.builder()
                .id(w.getWalletId())
                .customerId(w.getCustomer().getCustomerId())
                .balance(w.getBalance())
                .version(w.getVersion())
                .createdAt(w.getCreatedAt())
                .updatedAt(w.getUpdatedAt())
                .build();
    }

    private TransactionResponseDto toTxDto(Transaction t) {
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
