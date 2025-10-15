package com.example.miniwallet.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_tx_wallet_created", columnList = "wallet_id, created_at"),
    @Index(name = "idx_tx_transfer", columnList = "transfer_id"),
    @Index(name = "idx_tx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", nullable = false, length = 36, updatable = false)
    private String transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tx_wallet"))
    private Wallet wallet;

    // Add walletId field for Spring Data JPA query methods
    @Column(name = "wallet_id", nullable = false, insertable = false, updatable = false)
    private Long walletId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 6)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 9)
    private TransactionStatus status;

    @Column(name = "transfer_id", length = 36)
    private String transferId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_wallet_id", foreignKey = @ForeignKey(name = "fk_tx_related_wallet"))
    private Wallet relatedWallet;

    @Column(name = "reference", length = 255)
    private String reference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;

    public enum TransactionType {
        CREDIT, DEBIT
    }

    public enum TransactionStatus {
        INITIATED, SUCCESS, FAILED
    }
}
