CREATE DATABASE IF NOT EXISTS mini_wallet
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;
USE mini_wallet;

-- Use InnoDB for transactions / foreign keys
-- Customers
CREATE TABLE customers (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255),
  email VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- Wallets (one-to-one with customer)
CREATE TABLE wallets (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT UNSIGNED NOT NULL UNIQUE, -- 1:1
  balance DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
  version INT NOT NULL DEFAULT 0, -- maps to @Version optimistic locking in JPA
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_wallet_customer FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Transactions
CREATE TABLE transactions (
  id CHAR(36) NOT NULL PRIMARY KEY,             -- store UUID as CHAR(36)
  wallet_id BIGINT UNSIGNED NOT NULL,           -- wallet that this transaction belongs to
  amount DECIMAL(19,4) NOT NULL,
  type ENUM('CREDIT','DEBIT') NOT NULL,
  status ENUM('INITIATED','SUCCESS','FAILED') NOT NULL,
  transfer_id CHAR(36) NULL,                    -- optional: group of txns for a transfer
  related_wallet_id BIGINT UNSIGNED NULL,       -- counterparty wallet for transfers
  reference VARCHAR(255) NULL,                  -- external reference (payment id, etc.)
  created_at DATETIME(6) NOT NULL DEFAULT (CURRENT_TIMESTAMP(6)),
  CONSTRAINT fk_tx_wallet FOREIGN KEY (wallet_id) REFERENCES wallets(id) ON DELETE CASCADE,
  CONSTRAINT fk_tx_related_wallet FOREIGN KEY (related_wallet_id) REFERENCES wallets(id) ON DELETE SET NULL,
  INDEX idx_tx_wallet_created (wallet_id, created_at),
  INDEX idx_tx_transfer (transfer_id),
  INDEX idx_tx_status (status)
) ENGINE=InnoDB;

-- Customers
INSERT INTO customers (name, email) VALUES
  ('Rakesh', 'rakesh@example.com'),
  ('Anita',  'anita@example.com');

-- Wallets created for each customer
INSERT INTO wallets (customer_id, balance) VALUES
  (1, 1000.00),
  (2, 250.50);

-- Example transactions
INSERT INTO transactions (id, wallet_id, amount, type, status, created_at)
VALUES
  (UUID(), 1, 1000.00, 'CREDIT', 'SUCCESS', NOW(6)),
  (UUID(), 2, 250.50, 'CREDIT', 'SUCCESS', NOW(6));


DELIMITER $$

CREATE PROCEDURE transfer_funds(
  IN p_from_wallet BIGINT,
  IN p_to_wallet BIGINT,
  IN p_amount DECIMAL(19,4),
  IN p_reference VARCHAR(255)
)
BEGIN
  DECLARE v_balance DECIMAL(19,4);
  DECLARE v_transfer_id CHAR(36) DEFAULT UUID();
  DECLARE v_txid1 CHAR(36);
  DECLARE v_txid2 CHAR(36);

  -- Basic validations
  IF p_from_wallet = p_to_wallet THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SAME_WALLET';
  END IF;

  IF p_amount <= 0 THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INVALID_AMOUNT';
  END IF;

  START TRANSACTION;
    -- Lock source wallet row
    SELECT balance INTO v_balance FROM wallets WHERE id = p_from_wallet FOR UPDATE;
    IF v_balance IS NULL THEN
      ROLLBACK;
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'SOURCE_WALLET_NOT_FOUND';
    END IF;

    -- Check sufficient funds
    IF v_balance < p_amount THEN
      ROLLBACK;
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'INSUFFICIENT_FUNDS';
    END IF;

    -- Lock destination wallet row (ensure it exists) - optional to lock both
    SELECT balance INTO v_balance FROM wallets WHERE id = p_to_wallet FOR UPDATE;
    IF v_balance IS NULL THEN
      ROLLBACK;
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'DEST_WALLET_NOT_FOUND';
    END IF;

    -- Perform updates
    UPDATE wallets
      SET balance = balance - p_amount,
          version = version + 1
      WHERE id = p_from_wallet;

    UPDATE wallets
      SET balance = balance + p_amount,
          version = version + 1
      WHERE id = p_to_wallet;

    -- Insert transaction rows (two entries: debit & credit)
    SET v_txid1 = UUID();
    SET v_txid2 = UUID();

    INSERT INTO transactions (id, wallet_id, amount, type, status, transfer_id, related_wallet_id, reference, created_at)
    VALUES (v_txid1, p_from_wallet, p_amount, 'DEBIT', 'SUCCESS', v_transfer_id, p_to_wallet, p_reference, NOW(6));

    INSERT INTO transactions (id, wallet_id, amount, type, status, transfer_id, related_wallet_id, reference, created_at)
    VALUES (v_txid2, p_to_wallet, p_amount, 'CREDIT', 'SUCCESS', v_transfer_id, p_from_wallet, p_reference, NOW(6));

  COMMIT;
END$$

DELIMITER ;
