# MiniWallet API - Postman Collection Documentation

## Overview

This document provides comprehensive testing documentation for the MiniWallet API, including a complete Postman collection and automated test suite for implementation testing.

## API Endpoints Summary

### Customer Management
- **POST** `/api/customers` - Create a new customer
- **GET** `/api/customers/{id}` - Retrieve customer by ID
- **PUT** `/api/customers/{id}` - Update customer information
- **DELETE** `/api/customers/{id}` - Delete a customer

### Wallet Management
- **POST** `/api/wallets?customerId={id}` - Create wallet for customer
- **GET** `/api/wallets/{id}` - Retrieve wallet by ID
- **GET** `/api/wallets/by-customer/{customerId}` - Get wallet by customer ID
- **POST** `/api/wallets/{id}/deposit` - Deposit money into wallet
- **POST** `/api/wallets/{id}/withdraw` - Withdraw money from wallet
- **POST** `/api/wallets/transfer` - Transfer money between wallets

### Transaction Management
- **GET** `/api/transactions/{id}` - Retrieve transaction by ID
- **GET** `/api/transactions/by-wallet/{walletId}` - Get transactions by wallet (paginated)
- **GET** `/api/transactions/by-reference/{reference}` - Get transactions by reference
- **GET** `/api/transactions/by-transfer/{transferId}` - Get transactions by transfer ID

## Files Included

1. **`MiniWallet_Postman_Collection.json`** - Complete Postman collection with all endpoints
2. **`MiniWallet_API_Tests.json`** - Automated test suite with comprehensive scenarios
3. **`MiniWallet_Postman_Documentation.md`** - This documentation file

## Setup Instructions

### Prerequisites

1. **Java 11+** - Required for running the Spring Boot application
2. **Maven** - For building the application
3. **Postman** - For importing and running the collection
4. **Newman** (Optional) - For command-line test execution

### Application Setup

1. **Start the Application**
   ```bash
   cd /path/to/miniwallet
   mvn spring-boot:run
   ```

2. **Verify Application is Running**
   - Application should be accessible at `http://localhost:8080`
   - Swagger UI available at `http://localhost:8080/swagger-ui.html`

### Postman Setup

1. **Import the Collection**
   - Open Postman
   - Click "Import" button
   - Select `MiniWallet_Postman_Collection.json`
   - Collection will appear in your workspace

2. **Configure Environment Variables**
   - Create a new environment in Postman
   - Add variable: `baseUrl` with value `http://localhost:8080`
   - Set the environment as active

## Using the Postman Collection

### Manual Testing

1. **Customer Operations**
   - Navigate to "Customer Management" folder
   - Execute requests in order:
     1. Create Customer
     2. Get Customer by ID
     3. Update Customer
     4. Delete Customer

2. **Wallet Operations**
   - Create a customer first (use Customer Management requests)
   - Navigate to "Wallet Management" folder
   - Execute requests in order:
     1. Create Wallet for Customer
     2. Get Wallet by ID
     3. Deposit Money
     4. Withdraw Money
     5. Transfer Money (requires two wallets)

3. **Transaction Operations**
   - Execute wallet operations first to generate transactions
   - Navigate to "Transaction Management" folder
   - Test various filtering options

### Request/Response Examples

#### Create Customer
```json
// Request
POST {{baseUrl}}/api/customers
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@example.com"
}

// Response (201 Created)
{
  "id": 1,
  "name": "John Doe",
  "email": "john.doe@example.com",
  "createdAt": "2023-10-15T11:30:00",
  "updatedAt": "2023-10-15T11:30:00"
}
```

#### Deposit Money
```json
// Request
POST {{baseUrl}}/api/wallets/1/deposit
Content-Type: application/json

{
  "amount": 100.50,
  "reference": "Initial deposit"
}

// Response (201 Created)
{
  "id": "tx_001",
  "walletId": 1,
  "amount": 100.50,
  "type": "CREDIT",
  "status": "COMPLETED",
  "transferId": null,
  "relatedWalletId": null,
  "reference": "Initial deposit",
  "createdAt": "2023-10-15T11:45:00"
}
```

## Automated Testing

### Using Newman (Command Line)

1. **Install Newman**
   ```bash
   npm install -g newman
   ```

2. **Install HTML Reporter** (for detailed reports)
   ```bash
   npm install -g newman-reporter-html
   ```

3. **Run the Test Collection**
   ```bash
   newman run MiniWallet_API_Tests.json \
     --environment baseUrl=http://localhost:8080 \
     --reporters cli,json,html \
     --reporter-html-export test-results.html
   ```

4. **Run with Custom Environment File**
   ```bash
   newman run MiniWallet_API_Tests.json \
     -e MiniWallet_Environment.json \
     --reporters cli,html \
     --reporter-html-export results.html
   ```

### Test Scenarios Covered

#### Setup Tests
- ✅ Create test customers
- ✅ Create wallets for customers
- ✅ Verify initial wallet state (zero balance)

#### Core Functionality Tests
- ✅ Deposit money into wallet
- ✅ Verify balance updates after deposit
- ✅ Withdraw money from wallet
- ✅ Verify balance updates after withdrawal
- ✅ Transfer money between wallets
- ✅ Verify both wallets updated correctly after transfer

#### Transaction Management Tests
- ✅ Retrieve transaction by ID
- ✅ Get paginated transactions by wallet
- ✅ Filter transactions by reference
- ✅ Filter transactions by transfer ID
- ✅ Filter transactions by status and type

#### Error Handling Tests
- ✅ Insufficient funds scenario
- ✅ Customer not found (404)
- ✅ Wallet not found (404)

#### Validation Tests
- ✅ Invalid email format
- ✅ Negative amount validation
- ✅ Missing required fields

#### Cleanup Tests
- ✅ Delete test customers
- ✅ Verify cleanup completion

## Test Data

The automated tests use the following test data:

### Test Customers
- **Customer 1**: "Test Customer" (test.customer@example.com)
- **Customer 2**: "Second Customer" (second.customer@example.com)

### Test Amounts
- **Deposit**: $1000.50
- **Withdrawal**: $250.75
- **Transfer**: $100.00

### Expected Final Balances
- **Wallet 1**: $649.75 (1000.50 - 250.75 - 100.00)
- **Wallet 2**: $600.00 (500.00 + 100.00)

## Environment Configuration

### Postman Environment Variables

```json
{
  "baseUrl": "http://localhost:8080",
  "customerId": "",
  "walletId": "",
  "secondWalletId": "",
  "transactionId": "",
  "transferId": "test-transfer-001"
}
```

### Newman Environment File

Create a file named `miniwallet-env.json`:

```json
{
  "id": "miniwallet-env",
  "name": "MiniWallet Environment",
  "values": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "enabled": true
    }
  ]
}
```

## Running Tests in Different Environments

### Development Environment
```bash
newman run MiniWallet_API_Tests.json \
  --environment miniwallet-env.json \
  --delay-request 1000
```

### Staging Environment
```bash
newman run MiniWallet_API_Tests.json \
  --environment staging-env.json \
  --delay-request 1500
```

### Production Environment (Read-only tests)
```bash
newman run MiniWallet_API_Tests.json \
  --environment production-env.json \
  --suppress-tests "Cleanup"
```

## Troubleshooting

### Common Issues

1. **Application Not Running**
   - Ensure Spring Boot application is started
   - Check if port 8080 is available
   - Verify database connection

2. **Import Errors**
   - Ensure JSON files are valid
   - Check Postman version compatibility
   - Try re-importing the collection

3. **Test Failures**
   - Check if application is running
   - Verify database is clean/reset
   - Check environment variables
   - Review application logs for errors

4. **Newman Issues**
   - Ensure Newman is properly installed
   - Check Node.js version compatibility
   - Verify JSON syntax in collection files

### Debug Mode

Run tests with verbose output:
```bash
newman run MiniWallet_API_Tests.json \
  --verbose \
  --delay-request 2000
```

## Best Practices

### Testing Workflow

1. **Setup**: Create test data (customers, wallets)
2. **Execute**: Run core business logic tests
3. **Verify**: Check data consistency and balances
4. **Error Test**: Validate error handling
5. **Cleanup**: Remove test data

### Test Data Management

- Use unique identifiers for test entities
- Clean up test data after test completion
- Avoid dependencies between tests
- Use descriptive reference fields

### Performance Testing

For load testing, consider:
- Increasing delay between requests
- Running tests with multiple iterations
- Monitoring application performance metrics

## API Response Status Codes

### Success Codes
- **200 OK** - Request successful
- **201 Created** - Resource created successfully
- **204 No Content** - Request processed, no content returned

### Error Codes
- **400 Bad Request** - Invalid request data
- **404 Not Found** - Resource not found
- **422 Unprocessable Entity** - Validation errors
- **500 Internal Server Error** - Server error

## Support

For issues or questions regarding this API testing setup:

1. Check application logs for detailed error messages
2. Verify all prerequisites are installed correctly
3. Ensure test environment matches production setup
4. Review this documentation for setup instructions

## Version History

- **v1.0.0** - Initial release with complete API coverage
- **v1.1.0** - Added comprehensive error handling tests
- **v1.2.0** - Enhanced validation and edge case coverage

---

*This documentation was generated for MiniWallet API testing implementation.*
