# MiniWallet API Testing Guide - Step by Step

## Overview
This guide provides step-by-step instructions for testing the MiniWallet API using Postman. It includes all necessary URLs, JSON data, and testing procedures.

## Files Included
1. **MiniWallet_API_URLs.txt** - Complete list of API endpoints with full URLs
2. **MiniWallet_API_JSON_Data.json** - JSON request/response examples for all endpoints
3. **MiniWallet_Postman_Collection.json** - Ready-to-import Postman collection (if available)

## Prerequisites
1. **Java Development Environment** - JDK 11 or higher
2. **Postman** - Latest version installed
3. **MiniWallet Application** - Running on localhost:8080

## Step 1: Start the MiniWallet Application

1. Open terminal/command prompt
2. Navigate to the project directory:
   ```bash
   cd c:\Users\Rakesh\Projects\MiniWallet\miniwallet
   ```
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Wait for the application to start (you should see "Started MiniwalletApplication" in the console)
5. Verify the application is running by visiting: http://localhost:8080/swagger-ui.html

## Step 2: Set Up Postman

1. Open Postman application
2. Create a new tab or use an existing workspace
3. Set up environment variables (optional but recommended):
   - Click on "Environments" in the left sidebar
   - Create new environment: "MiniWallet Local"
   - Add variables:
     - `base_url` = `http://localhost:8080`
     - `customer_id` = `1` (will be updated after creating customer)
     - `wallet_id` = `1` (will be updated after creating wallet)

## Step 3: Test Customer Endpoints

### 3.1 Create Customer
1. **URL**: `http://localhost:8080/api/customers`
2. **Method**: POST
3. **Headers**:
   - `Content-Type: application/json`
4. **Body** (raw JSON):
   ```json
   {
     "name": "John Doe",
     "email": "john.doe@example.com"
   }
   ```
5. **Expected Response**:
   ```json
   {
     "id": 1,
     "name": "John Doe",
     "email": "john.doe@example.com",
     "createdAt": "2024-01-15T10:30:00",
     "updatedAt": "2024-01-15T10:30:00"
   }
   ```
6. **Actions**:
   - Click "Send"
   - Copy the returned `id` value
   - Update your environment variable `customer_id` with this value

### 3.2 Get Customer by ID
1. **URL**: `http://localhost:8080/api/customers/1` (replace 1 with your customer ID)
2. **Method**: GET
3. **Headers**: None required
4. **Expected Response**: Customer details as shown above
5. **Actions**:
   - Click "Send"
   - Verify the response matches the data you entered

### 3.3 Update Customer
1. **URL**: `http://localhost:8080/api/customers/1` (replace 1 with your customer ID)
2. **Method**: PUT
3. **Headers**:
   - `Content-Type: application/json`
4. **Body** (raw JSON):
   ```json
   {
     "name": "John Smith",
     "email": "john.smith@example.com"
   }
   ```
5. **Expected Response**: Updated customer information
6. **Actions**:
   - Click "Send"
   - Verify the name and email are updated

## Step 4: Test Wallet Endpoints

### 4.1 Create Wallet for Customer
1. **URL**: `http://localhost:8080/api/wallets?customerId=1` (replace 1 with your customer ID)
2. **Method**: POST
3. **Headers**: None required
4. **Expected Response**:
   ```json
   {
     "id": 1,
     "customerId": 1,
     "balance": 0.00,
     "version": 0,
     "createdAt": "2024-01-15T10:35:00",
     "updatedAt": "2024-01-15T10:35:00"
   }
   ```
5. **Actions**:
   - Click "Send"
   - Copy the returned `id` value
   - Update your environment variable `wallet_id` with this value

### 4.2 Get Wallet by ID
1. **URL**: `http://localhost:8080/api/wallets/1` (replace 1 with your wallet ID)
2. **Method**: GET
3. **Expected Response**: Wallet details with balance 0.00

### 4.3 Deposit Money
1. **URL**: `http://localhost:8080/api/wallets/1/deposit` (replace 1 with your wallet ID)
2. **Method**: POST
3. **Headers**:
   - `Content-Type: application/json`
4. **Body** (raw JSON):
   ```json
   {
     "amount": 1000.00,
     "reference": "Initial deposit"
   }
   ```
5. **Expected Response**: Transaction details with type "DEPOSIT"
6. **Actions**:
   - Click "Send"
   - Note the transaction ID for future reference

### 4.4 Withdraw Money
1. **URL**: `http://localhost:8080/api/wallets/1/withdraw` (replace 1 with your wallet ID)
2. **Method**: POST
3. **Headers**:
   - `Content-Type: application/json`
4. **Body** (raw JSON):
   ```json
   {
     "amount": 200.00,
     "reference": "ATM withdrawal"
   }
   ```
5. **Expected Response**: Transaction details with type "WITHDRAWAL"

## Step 5: Test Transaction Endpoints

### 5.1 Get Transaction by ID
1. **URL**: `http://localhost:8080/api/transactions/TXN-001` (replace with actual transaction ID)
2. **Method**: GET
3. **Expected Response**: Transaction details

### 5.2 Get Transactions by Wallet
1. **URL**: `http://localhost:8080/api/transactions/by-wallet/1` (replace 1 with your wallet ID)
2. **Method**: GET
3. **Optional Query Parameters**:
   - `page`: 0 (default)
   - `size`: 20 (default)
   - `status`: COMPLETED
   - `type`: DEPOSIT
   - `start`: 2024-01-01T00:00:00
   - `end`: 2024-12-31T23:59:59

### 5.3 Get Transactions by Reference
1. **URL**: `http://localhost:8080/api/transactions/by-reference/Initial deposit`
2. **Method**: GET
3. **Expected Response**: Array of transactions with matching reference

## Step 6: Test Transfer Endpoint

### 6.1 Create Second Customer and Wallet
1. Create another customer:
   - **URL**: `http://localhost:8080/api/customers`
   - **Method**: POST
   - **Body**:
     ```json
     {
       "name": "Jane Doe",
       "email": "jane.doe@example.com"
     }
     ```
2. Create wallet for second customer:
   - **URL**: `http://localhost:8080/api/wallets?customerId=2`
   - **Method**: POST

### 6.2 Deposit Money to Second Wallet
1. **URL**: `http://localhost:8080/api/wallets/2/deposit`
2. **Method**: POST
3. **Body**:
   ```json
   {
     "amount": 500.00,
     "reference": "Initial deposit"
   }
   ```

### 6.3 Transfer Money Between Wallets
1. **URL**: `http://localhost:8080/api/wallets/transfer`
2. **Method**: POST
3. **Headers**:
   - `Content-Type: application/json`
4. **Body**:
   ```json
   {
     "fromWalletId": 1,
     "toWalletId": 2,
     "amount": 100.00,
     "transferId": "TRANSFER-001",
     "reference": "Payment for services"
   }
   ```
5. **Expected Response**: Array of two transactions (debit and credit)

## Step 7: Test Error Scenarios

### 7.1 Test Validation Errors
1. Try to create a customer with invalid email:
   ```json
   {
     "name": "John Doe",
     "email": "invalid-email"
   }
   ```
2. Expected: 400 Bad Request with validation error message

### 7.2 Test Insufficient Funds
1. Try to withdraw more money than available in wallet
2. Expected: 400 Bad Request with "Insufficient funds" message

### 7.3 Test Non-existent Resource
1. Try to get customer with ID 99999
2. Expected: 404 Not Found

## Step 8: Advanced Testing Features

### 8.1 Filter Transactions by Date Range
```
GET http://localhost:8080/api/transactions/by-wallet/1?start=2024-01-01T00:00:00&end=2024-12-31T23:59:59
```

### 8.2 Paginated Results
```
GET http://localhost:8080/api/transactions/by-wallet/1?page=0&size=10
```

### 8.3 Filter by Transaction Status
```
GET http://localhost:8080/api/transactions/by-wallet/1?status=COMPLETED
```

## Step 9: Clean Up (Optional)

### 9.1 Delete Test Data
1. Delete customers (this will also delete associated wallets and transactions):
   ```
   DELETE http://localhost:8080/api/customers/1
   DELETE http://localhost:8080/api/customers/2
   ```

## Troubleshooting

### Common Issues:

1. **Application not starting**:
   - Check if port 8080 is already in use
   - Verify Java version (JDK 11+ required)
   - Check application.properties for database configuration

2. **Connection refused**:
   - Ensure the Spring Boot application is running
   - Check if you're using the correct port (8080)

3. **JSON parsing errors**:
   - Verify JSON syntax in request body
   - Ensure proper Content-Type header

4. **Validation errors**:
   - Check email format (must be valid email)
   - Ensure amount is greater than 0.0001
   - Verify required fields are provided

## Tips for Effective Testing

1. **Save requests in Postman collections** for reuse
2. **Use environment variables** for dynamic values (IDs, tokens)
3. **Test one endpoint at a time** following the logical flow
4. **Verify responses** match expected results
5. **Test error scenarios** to ensure proper error handling
6. **Use different data sets** to test various scenarios
7. **Document any issues** or unexpected behaviors

## Next Steps

1. Explore the Swagger UI documentation at: http://localhost:8080/swagger-ui.html
2. Test edge cases and boundary conditions
3. Set up automated tests using the JSON data provided
4. Explore additional query parameters for transaction filtering

## Support

If you encounter any issues:
1. Check the application logs in the terminal
2. Verify all prerequisites are met
3. Ensure proper JSON formatting in requests
4. Check the Swagger documentation for detailed API specifications
