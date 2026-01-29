# Wallet API

A high-performance REST API for wallet operations supporting deposit and withdrawal transactions with concurrent request handling (up to 1000 RPS per wallet).

## Features

- ✅ RESTful API for wallet operations (deposit/withdraw)
- ✅ High concurrency support with pessimistic locking
- ✅ Transaction history tracking
- ✅ PostgreSQL database with Liquibase migrations
- ✅ Docker & Docker Compose support
- ✅ Environment-based configuration
- ✅ Comprehensive error handling
- ✅ Unit and integration tests
- ✅ Spring Boot 3 with Java 17

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.2**
- **Spring Data JPA**
- **PostgreSQL 15**
- **Liquibase**
- **Docker & Docker Compose**
- **Maven**

## API Endpoints

### Wallet Operations

#### Deposit or Withdraw
```
POST /api/v1/wallet
Content-Type: application/json

{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "operationType": "DEPOSIT",
  "amount": 1000.00
}
```

**Response (200 OK):**
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.00,
  "timestamp": 1705003200000
}
```

#### Get Wallet Balance
```
GET /api/v1/wallets/{walletId}
```

**Response (200 OK):**
```json
{
  "walletId": "550e8400-e29b-41d4-a716-446655440000",
  "balance": 1500.00,
  "timestamp": 1705003200000
}
```

## Error Responses

### Wallet Not Found (404)
```json
{
  "status": 404,
  "code": "WALLET_NOT_FOUND",
  "message": "Wallet not found: 550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1705003200000
}
```

### Insufficient Funds (400)
```json
{
  "status": 400,
  "code": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds. Available: 500.00, Requested: 1000.00",
  "timestamp": 1705003200000
}
```

### Validation Error (400)
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

## Quick Start with Docker Compose

### Prerequisites
- Docker and Docker Compose installed
- At least 2GB available memory

### Start the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd wallet-api
```

2. Configure environment variables (optional):
```bash
# Edit .env file to customize settings
cat .env
```

3. Start the services:
```bash
docker-compose up -d
```

4. Verify the application is running:
```bash
curl -X GET http://localhost:8080/actuator/health
```

### Stop the Application
```bash
docker-compose down
```

## Local Development Setup

### Prerequisites
- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15

### Build and Run

1. Clone the repository:
```bash
git clone <repository-url>
cd wallet-api
```

2. Install dependencies:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

4. The API will be available at `http://localhost:8080`

## Running Tests

### Unit Tests
```bash
mvn test
```

### Integration Tests (requires Docker)
```bash
mvn test -P integration
```

### All Tests
```bash
mvn verify
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_HOST` | localhost | Database host |
| `DB_PORT` | 5432 | Database port |
| `DB_NAME` | wallet_db | Database name |
| `DB_USER` | wallet_user | Database user |
| `DB_PASSWORD` | wallet_password | Database password |
| `DB_POOL_SIZE` | 20 | Maximum connection pool size |
| `DB_MIN_IDLE` | 5 | Minimum idle connections |
| `SERVER_PORT` | 8080 | Application server port |

### Application Configuration

Edit `src/main/resources/application.yml` for Spring Boot configuration:

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # For high concurrency
      minimum-idle: 5
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 20
```

## Concurrency Handling

The application uses **pessimistic locking** to handle high concurrency (1000+ RPS):

1. Each withdrawal request acquires an exclusive lock on the wallet row
2. Balance is checked and updated atomically within a transaction
3. Database constraints prevent invalid states
4. Transaction rollback on validation failure

Key implementation details:
- `@Lock(LockModeType.PESSIMISTIC_WRITE)` on wallet retrieval
- Optimistic version control for conflict detection
- Connection pool sized for concurrent requests
- Database indexes on frequently accessed columns

## Database Schema

### Wallets Table
```sql
CREATE TABLE wallets (
  id UUID PRIMARY KEY,
  wallet_id UUID UNIQUE NOT NULL,
  balance NUMERIC(19,2) NOT NULL,
  version BIGINT NOT NULL,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);
```

### Transactions Table
```sql
CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  wallet_id UUID NOT NULL,
  operation_type VARCHAR(20) NOT NULL,
  amount NUMERIC(19,2) NOT NULL,
  balance_before NUMERIC(19,2) NOT NULL,
  balance_after NUMERIC(19,2) NOT NULL,
  created_at BIGINT NOT NULL
);
```

## Load Testing

Example load test using `curl`:

```bash
#!/bin/bash
WALLET_ID="550e8400-e29b-41d4-a716-446655440000"

for i in {1..1000}; do
  curl -X POST http://localhost:8080/api/v1/wallet \
    -H "Content-Type: application/json" \
    -d '{
      "walletId": "'$WALLET_ID'",
      "operationType": "DEPOSIT",
      "amount": 1.00
    }' &
done
wait
```

## Architecture

```
wallet-api/
├── src/main/
│   ├── java/com/wallet/
│   │   ├── WalletApplication.java       # Main application class
│   │   ├── controller/                  # REST endpoints
│   │   ├── service/                     # Business logic
│   │   ├── repository/                  # Data access layer
│   │   ├── entity/                      # JPA entities
│   │   ├── dto/                         # Data transfer objects
│   │   └── exception/                   # Custom exceptions
│   └── resources/
│       ├── application.yml              # Main configuration
│       ├── application-test.yml         # Test configuration
│       └── db/changelog/                # Liquibase migrations
├── src/test/                            # Test classes
├── Dockerfile                           # Container image definition
├── docker-compose.yml                   # Multi-container setup
├── .env                                 # Environment variables
└── pom.xml                              # Maven configuration
```

## Project Status

✅ Complete implementation with:
- Core functionality implemented
- Full test coverage
- Docker containerization
- Production-ready configuration
- Error handling and validation
- High concurrency support

## License

This project is provided as-is for educational purposes.

## Support

For issues or questions, please refer to the GitHub repository issues section.
