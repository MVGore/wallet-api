# API Documentation

## Overview

The Wallet API is a high-performance REST API for managing wallet operations including deposits and withdrawals. It's designed to handle high concurrency (1000+ RPS per wallet) with proper error handling and transaction tracking.

## Base URL

```
http://localhost:8080/api/v1
```

## Authentication

Currently, the API does not require authentication. In production, implement API key or OAuth2 authentication.

## Request/Response Format

All requests and responses use JSON format.

### Common Headers

```
Content-Type: application/json
Accept: application/json
```

## Endpoints

### 1. Process Wallet Operation

Deposit or withdraw funds from a wallet.

**Endpoint:** `POST /wallet`

**Request Body:**
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000.50
}
```

**Request Parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| walletId | UUID | Yes | Unique wallet identifier |
| operationType | Enum | Yes | Operation type: `DEPOSIT` or `WITHDRAW` |
| amount | Decimal | Yes | Amount to deposit/withdraw (must be > 0) |

**Response (200 OK):**
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.50,
  "timestamp": 1705003200000
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| walletId | UUID | The wallet ID |
| balance | Decimal | Current wallet balance after operation |
| timestamp | Long | Unix timestamp in milliseconds |

**Error Responses:**

- **400 Bad Request** - Invalid input or insufficient funds
- **404 Not Found** - Wallet not found
- **500 Internal Server Error** - Unexpected error

#### Example cURL Requests

**Deposit:**
```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "DEPOSIT",
    "amount": 1000.00
  }'
```

**Withdraw:**
```bash
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "550e8400-e29b-41d4-a716-446655440000",
    "operationType": "WITHDRAW",
    "amount": 500.00
  }'
```

### 2. Get Wallet Balance

Retrieve the current balance of a wallet.

**Endpoint:** `GET /wallets/{walletId}`

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| walletId | UUID | Wallet identifier |

**Response (200 OK):**
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.50,
  "timestamp": 1705003200000
}
```

**Error Responses:**

- **404 Not Found** - Wallet not found
- **500 Internal Server Error** - Unexpected error

#### Example cURL Request

```bash
curl -X GET http://localhost:8080/api/v1/wallets/550e8400-e29b-41d4-a716-446655440000
```

## Error Responses

### Error Response Format

All error responses follow this format:

```json
{
  "status": 400,
  "code": "ERROR_CODE",
  "message": "Error description",
  "timestamp": 1705003200000,
  "errors": {
    "field": "Error details"
  }
}
```

### Error Codes

| Code | HTTP Status | Description |
|------|-------------|-------------|
| `WALLET_NOT_FOUND` | 404 | Wallet does not exist |
| `INSUFFICIENT_FUNDS` | 400 | Insufficient balance for withdrawal |
| `VALIDATION_ERROR` | 400 | Invalid request format or validation failure |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

### Example Error Responses

**Wallet Not Found:**
```json
{
  "status": 404,
  "code": "WALLET_NOT_FOUND",
  "message": "Wallet not found: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1705003200000
}
```

**Insufficient Funds:**
```json
{
  "status": 400,
  "code": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds. Available: 500.00, Requested: 1000.00",
  "timestamp": 1705003200000
}
```

**Validation Error:**
```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request format",
  "timestamp": 1705003200000,
  "errors": {
    "amount": "Amount must be greater than 0",
    "walletId": "Wallet ID cannot be null"
  }
}
```

## Data Types

### UUID
Format: RFC 4122 UUID string
Example: `550e8400-e29b-41d4-a716-446655440000`

### Decimal
Numeric values with up to 2 decimal places
Example: `1000.50`, `0.01`, `999999.99`

### Timestamp
Unix timestamp in milliseconds (long)
Example: `1705003200000`

## Rate Limiting

The API supports high concurrency (1000+ RPS per wallet). For each wallet:

- Concurrent requests are handled with pessimistic locking
- Each request acquires exclusive lock on wallet row during transaction
- Requests are processed sequentially per wallet to ensure data consistency
- No additional rate limiting is implemented

## Pagination

Pagination is not applicable for these endpoints.

## Sorting

Sorting is not applicable for these endpoints.

## Filtering

Filtering is not applicable for these endpoints.

## Versioning

The API version is included in the URL path: `/api/v1`

Current version: `v1`

## Changes and Deprecations

### Planned Features (v2)

- Batch operations for multiple transactions
- Transaction history retrieval
- Scheduled transactions
- Wallet groups
- Advanced reporting

## Testing

### Using httpie

```bash
# Deposit
http POST http://localhost:8080/api/v1/wallet \
  walletId=550e8400-e29b-41d4-a716-446655440000 \
  operationType=DEPOSIT \
  amount:=1000

# Get Balance
http GET http://localhost:8080/api/v1/wallets/550e8400-e29b-41d4-a716-446655440000
```

### Using Python requests

```python
import requests
import uuid

BASE_URL = "http://localhost:8080/api/v1"
WALLET_ID = str(uuid.uuid4())

# Deposit
response = requests.post(
    f"{BASE_URL}/wallet",
    json={
        "walletId": WALLET_ID,
        "operationType": "DEPOSIT",
        "amount": 1000.00
    }
)
print(response.json())

# Get Balance
response = requests.get(
    f"{BASE_URL}/wallets/{WALLET_ID}"
)
print(response.json())
```

## FAQ

**Q: What happens if I exceed the database connection pool?**
A: The application will return a 500 error after timeout. Increase `DB_POOL_SIZE` if this occurs frequently.

**Q: Can I withdraw negative amounts?**
A: No, the API validates that amount must be greater than 0.

**Q: What if a wallet doesn't exist?**
A: The API returns 404 Not Found. Create a wallet with an initial deposit first.

**Q: Are transactions atomic?**
A: Yes, each operation is wrapped in a database transaction with pessimistic locking.

**Q: Can I see transaction history?**
A: Currently not available through the API, but transactions are recorded in the database.

## Support

For issues or questions, please refer to the GitHub repository or contact the development team.
