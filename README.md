

### 🔹 Project Name: **Mini Wallet Management System**

**Goal:**
To build a **simple yet robust digital wallet backend** that handles **money transactions safely, atomically, and consistently** — just like a real e-wallet system (Paytm, PhonePe, etc.), but simplified for learning and demonstration.

---

## 💡 **Primary Use Case**

The system allows **customers** to:

1. **Create a wallet** linked to their profile.
2. **Add money (credit)** into their wallet.
3. **Spend or withdraw (debit)** money.
4. **Transfer money** between wallets.
5. **View all past transactions** (ledger/history).

Everything happens **within the same database transaction** so that **no money is lost or duplicated**, even if two operations occur at the same time (concurrency-safe).

---

## ⚙️ **Technical Goals / Learning Objectives**

| Concept                       | Explanation                                                                                     |
| ----------------------------- | ----------------------------------------------------------------------------------------------- |
| **Transactional Consistency** | Using `@Transactional` ensures debit and credit operations either *both happen* or *both fail*. |
| **Optimistic Locking**        | Prevents double spending when multiple users access the same wallet simultaneously.             |
| **Idempotency**               | Ensures that reprocessing the same transaction doesn’t duplicate results.                       |
| **Layered Architecture**      | Clean separation between controller, service, repository, and entity layers.                    |
| **Optional Caching (Redis)**  | Speeds up balance reads and reduces DB hits.                                                    |
| **Scalability-Ready Design**  | Can later evolve into microservices, add Kafka, JWT, etc.                                       |

---

## 🧩 **Core Entities and Their Roles**

| Entity          | Description                                                                           |
| --------------- | ------------------------------------------------------------------------------------- |
| **Customer**    | Represents the end user of the wallet system.                                         |
| **Wallet**      | Holds the current balance of each customer. Each customer has exactly **one** wallet. |
| **Transaction** | Records each debit, credit, or transfer action — forming an immutable ledger.         |

---

## 🔄 **System Flow (Step-by-Step)**

Let’s walk through the **main flow** of how things work in this project 👇

---

### 🧍 Step 1: Customer Onboarding

* Customer registers using:

  ```http
  POST /api/customers
  ```
* A `Customer` record is created in MySQL.
* Optionally, an initial `Wallet` is automatically created for the new customer.

---

### 💳 Step 2: Wallet Creation

* Either auto-created during registration **or** manually via:

  ```http
  POST /api/wallets/{customerId}/create
  ```
* Wallet starts with `balance = 0` (or configurable initial balance).
* `Wallet` is linked to the `Customer` via one-to-one mapping.

---

### 💰 Step 3: Add Money (Credit)

* When customer adds money:

  ```http
  POST /api/wallets/{walletId}/credit
  {
    "amount": 500.00
  }
  ```
* System:

  1. Fetches wallet.
  2. Adds amount to balance.
  3. Creates a **Transaction** of type `CREDIT`.
  4. Commits both balance update + transaction record **atomically** (`@Transactional`).

---

### 💸 Step 4: Spend or Withdraw (Debit)

* When customer makes a purchase or withdrawal:

  ```http
  POST /api/wallets/{walletId}/debit
  {
    "amount": 200.00
  }
  ```
* System:

  1. Checks if wallet has sufficient balance.
  2. Deducts amount.
  3. Records a **Transaction** of type `DEBIT`.
  4. If insufficient funds, it throws an error → no changes saved (atomic rollback).

---

### 🔁 Step 5: Transfer Between Wallets

* To send money from one customer to another:

  ```http
  POST /api/wallets/transfer
  {
    "fromWalletId": 1,
    "toWalletId": 2,
    "amount": 100.00
  }
  ```
* System:

  1. Starts a DB transaction.
  2. Debits sender’s wallet.
  3. Credits receiver’s wallet.
  4. Creates **two transaction records** (`DEBIT` & `CREDIT`).
  5. Commits all or none — if any step fails, rollback happens.

---

### 📜 Step 6: View Transaction History

* Customer can view past transactions:

  ```http
  GET /api/transactions/wallet/{walletId}
  ```
* Returns list of all transactions — acts as the **ledger** for audit or debugging.

---

![alt text](deepseek_mermaid_20251015_b7d422.png)

## 🧠 **How the Layers Work Together**

### 1. **Controller Layer**

* Handles HTTP requests and responses.
* Delegates logic to the service layer.
* Example: `WalletController.credit()` calls `WalletService.credit()`.

### 2. **Service Layer**

* Contains the **business logic**.
* Manages atomic operations with `@Transactional`.
* Example: `transfer()` ensures debit & credit happen together.

### 3. **Repository Layer**

* Handles database operations via Spring Data JPA.
* Example: `walletRepository.save(wallet)`.

### 4. **Model Layer**

* Defines entities and their relationships.
* Annotated with `@Entity`, `@OneToOne`, `@ManyToOne`, etc.

### 5. **Optional Layers**

* **DTOs** → for clean request/response objects.
* **Redis Cache** → to cache wallet balances.
* **Scheduler** → for retrying failed transactions.

---

## 🧩 **Simple Example Flow**

### 🎬 Example Scenario

1. Rakesh registers → gets Customer ID = 1
2. His wallet is created → Wallet ID = 101, balance = ₹0
3. He credits ₹1000 → balance = ₹1000
4. He buys something for ₹300 → balance = ₹700
5. Transfers ₹200 to his friend’s wallet → balance = ₹500
6. Can view all these transactions via `/api/transactions/wallet/101`

Everything is **atomic**, consistent, and fully logged.

---

## 🚀 **Future Expansion (Beyond Mini Version)**

Once this works perfectly, you can:

* Add **JWT Authentication** (using `UserDetailsService`).
* Add **Kafka** for async events.
* Split into **microservices** (WalletService, TransactionService).
* Add **API Gateway + Registry**.
* Move to **eventual consistency (Saga pattern)**.

