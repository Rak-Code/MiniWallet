// package com.example.miniwallet.service;
package com.example.miniwallet.service;

import com.example.miniwallet.entity.Transaction;
import com.example.miniwallet.entity.Wallet;
import com.example.miniwallet.entity.Customer;
import com.example.miniwallet.exception.BadRequestException;
import com.example.miniwallet.exception.InsufficientFundsException;
import com.example.miniwallet.exception.ResourceNotFoundException;
import com.example.miniwallet.exception.DuplicateResourceException;
import com.example.miniwallet.repository.TransactionRepository;
import com.example.miniwallet.repository.WalletRepository;
import com.example.miniwallet.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    private static BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null) throw new BadRequestException("amount is required");
        BigDecimal scaled = amount.setScale(4, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("amount must be > 0");
        }
        return scaled;
    }

    public Wallet createWalletForCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        if (walletRepository.existsByCustomerId(customerId)) {
            throw new DuplicateResourceException("Wallet already exists for customer: " + customerId);
        }

        Wallet wallet = Wallet.builder()
                .customer(customer)
                .balance(BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP))
                .build();

        return walletRepository.save(wallet);
    }

    @Cacheable(value = "wallets", key = "'wallet_' + #id")
    public Wallet getById(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + id));
    }

    @Cacheable(value = "wallets", key = "'wallet_customer_' + #customerId")
    public Optional<Wallet> findByCustomerId(Long customerId) {
        return walletRepository.findByCustomerId(customerId);
    }

    public List<Wallet> findAll() {
        return walletRepository.findAll();
    }

    @Transactional
    // Evict the wallet entry so subsequent getById fetches the updated balance from DB
    @CacheEvict(value = "wallets", key = "'wallet_' + #walletId")
    public Transaction deposit(Long walletId, BigDecimal amount, String reference) {
        BigDecimal amt = normalizeAmount(amount);

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        // update balance
        wallet.setBalance(wallet.getBalance().add(amt));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .amount(amt)
                .type(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .reference(reference)
                .build();

        return transactionRepository.save(tx);
    }

    @Transactional
    // Evict the wallet entry so subsequent getById fetches the updated balance from DB
    @CacheEvict(value = "wallets", key = "'wallet_' + #walletId")
    public Transaction withdraw(Long walletId, BigDecimal amount, String reference) {
        BigDecimal amt = normalizeAmount(amount);

        Wallet wallet = walletRepository.findByIdWithLock(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + walletId));

        if (wallet.getBalance().compareTo(amt) < 0) {
            throw new InsufficientFundsException("insufficient balance in wallet: " + walletId);
        }

        wallet.setBalance(wallet.getBalance().subtract(amt));
        walletRepository.save(wallet);

        Transaction tx = Transaction.builder()
                .wallet(wallet)
                .amount(amt)
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .reference(reference)
                .build();

        return transactionRepository.save(tx);
    }

    /**
     * Transfer amount from one wallet to another.
     * Creates two transactions (DEBIT on fromWallet, CREDIT on toWallet) that share transferId.
     *
     * Ensures ordered locking by wallet id to reduce deadlocks.
     */
    @Transactional
    // Evict affected wallets from unified 'wallets' cache so next getById fetches DB
    @Caching(evict = {
        @CacheEvict(value = "wallets", key = "'wallet_' + #fromWalletId"),
        @CacheEvict(value = "wallets", key = "'wallet_' + #toWalletId")
    })
    public List<Transaction> transfer(Long fromWalletId, Long toWalletId, BigDecimal amount, String transferId, String reference) {
        if (fromWalletId.equals(toWalletId)) {
            throw new BadRequestException("from and to wallet must be different");
        }

        // If client did not provide a transferId, generate one here (service-layer generation)
        if (transferId == null || transferId.isBlank()) {
            // generate a plain UUID (36 chars including hyphens) so it fits the DB column length
            transferId = java.util.UUID.randomUUID().toString();
        } else {
            // if client supplied transferId, ensure it's not already processed to maintain idempotency
            if (!transactionRepository.findByTransferId(transferId).isEmpty()) {
                throw new DuplicateResourceException("transferId already processed: " + transferId);
            }
        }

        BigDecimal amt = normalizeAmount(amount);

        // lock order by id - always lock smaller id first
        Wallet first, second;
        boolean fromIsFirst = fromWalletId < toWalletId;
        if (fromIsFirst) {
            first = walletRepository.findByIdWithLock(fromWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + fromWalletId));
            second = walletRepository.findByIdWithLock(toWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + toWalletId));
        } else {
            first = walletRepository.findByIdWithLock(toWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + toWalletId));
            second = walletRepository.findByIdWithLock(fromWalletId)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found: " + fromWalletId));
        }

        Wallet fromWallet = fromIsFirst ? first : second;
        Wallet toWallet = fromIsFirst ? second : first;

        // check balance
        if (fromWallet.getBalance().compareTo(amt) < 0) {
            throw new InsufficientFundsException("insufficient funds in wallet: " + fromWallet.getWalletId());
        }

        // perform balance updates
        fromWallet.setBalance(fromWallet.getBalance().subtract(amt));
        toWallet.setBalance(toWallet.getBalance().add(amt));

        // persist wallets
        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        // create transactions
        Transaction debit = Transaction.builder()
                .wallet(fromWallet)
                .amount(amt)
                .type(Transaction.TransactionType.DEBIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .transferId(transferId)
                .relatedWallet(toWallet)
                .reference(reference)
                .build();

        Transaction credit = Transaction.builder()
                .wallet(toWallet)
                .amount(amt)
                .type(Transaction.TransactionType.CREDIT)
                .status(Transaction.TransactionStatus.SUCCESS)
                .transferId(transferId)
                .relatedWallet(fromWallet)
                .reference(reference)
                .build();

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        return List.of(debit, credit);
    }
}
