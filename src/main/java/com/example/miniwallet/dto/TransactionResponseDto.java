package com.example.miniwallet.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDto {
    private String id;
    private Long walletId;
    private BigDecimal amount;
    private String type; // CREDIT / DEBIT
    private String status;
    private String transferId;
    private Long relatedWalletId;
    private String reference;
    private LocalDateTime createdAt;
}
