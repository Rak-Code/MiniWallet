package com.example.miniwallet.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletResponseDto {
    private Long id;
    private Long customerId;
    private BigDecimal balance;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
