# Wallet API - Complete Startup & Testing Guide

## Quick Start with Docker Compose (Recommended)

### Prerequisites
- Docker Desktop installed and running
- Docker Compose installed
- Port 8080 available (API)
- Port 5432 available (PostgreSQL)

### Step 1: Start the Application
```bash
cd wallet-api
docker-compose up -d
```

Expected output:
```
[+] Building 2.5s (15/15) FINISHED
[+] Running 2/2
  ✔ postgres  Healthy
  ✔ wallet-api Healthy
```

### Step 2: Wait for Services to Be Ready
```bash
# Wait about 40 seconds for services to fully initialize
# You can check health:
docker-compose ps

# Should show both services as "Up"
```

### Step 3: Run Tests

#### Option A: Using PowerShell (Windows)
```powershell
# Navigate to project directory
cd c:\Users\DELL\Documents\VS CODE\wallet-api

# Run the test script
.\test-api.ps1
```

#### Option B: Using Bash/Shell (Linux/Mac)
```bash
cd wallet-api
bash test-api.sh
```

#### Option C: Manual curl commands
```bash
# Create a wallet
curl -X POST http://localhost:8080/api/v1/wallets \
  -H "Content-Type: application/json"

# Get balance
curl -X GET http://localhost:8080/api/v1/wallets/{WALLET_ID}

# Deposit money
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "{WALLET_ID}",
    "operationType": "DEPOSIT",
    "amount": 5000
  }'

# Withdraw money
curl -X POST http://localhost:8080/api/v1/wallet \
  -H "Content-Type: application/json" \
  -d '{
    "walletId": "{WALLET_ID}",
    "operationType": "WITHDRAW",
    "amount": 1000
  }'
```

---

## Alternative: Run Without Docker (Local Development)

### Prerequisites
- Java 17+ installed
- Maven installed
- PostgreSQL 15 running locally

### Step 1: Build the Project
```bash
mvn clean package -DskipTests
```

### Step 2: Configure Database
Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/wallet_db
    username: wallet_user
    password: wallet_password
```

### Step 3: Run the Application
```bash
java -jar target/wallet-api-1.0.0.jar
```

Or using Maven:
```bash
mvn spring-boot:run
```

---

## Expected Output Examples

### Successful Wallet Creation
```json
{
  "walletId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "balance": 0
}
```

### Successful Deposit
```json
{
  "transactionId": "a1b2c3d4-e5f6-4g7h-8i9j-0k1l2m3n4o5p",
  "walletId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "operationType": "DEPOSIT",
  "amount": 5000,
  "newBalance": 5000,
  "timestamp": "2026-01-29T17:45:30.123456Z"
}
```

### Error Response - Insufficient Funds
```json
{
  "error": "INSUFFICIENT_FUNDS",
  "message": "Insufficient funds. Current balance: 3500, Requested amount: 5000",
  "timestamp": "2026-01-29T17:46:00Z"
}
```

### Error Response - Wallet Not Found
```json
{
  "error": "WALLET_NOT_FOUND",
  "message": "Wallet not found: 00000000-0000-0000-0000-000000000000",
  "timestamp": "2026-01-29T17:46:30Z"
}
```

---

## Stopping the Application

### Stop Docker Containers
```bash
docker-compose down

# To also remove volumes:
docker-compose down -v
```

### View Logs
```bash
# All logs
docker-compose logs

# Follow logs in real-time
docker-compose logs -f

# Last 50 lines of wallet-api logs
docker-compose logs wallet-api --tail=50
```

---

## Performance Testing (1000 RPS)

### Using Apache Bench (Linux/Mac)
```bash
# Single request test
ab -n 1000 -c 100 http://localhost:8080/api/v1/wallets

# POST request test
echo '{
  "walletId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "operationType": "DEPOSIT",
  "amount": 100
}' > /tmp/operation.json

ab -n 10000 -c 100 -p /tmp/operation.json \
  -T "application/json" \
  http://localhost:8080/api/v1/wallet
```

### Using wrk (High Performance)
```bash
# Install wrk: https://github.com/wg/wrk

# GET request performance
wrk -t4 -c100 -d30s http://localhost:8080/api/v1/wallets

# POST request (requires Lua script)
wrk -t4 -c100 -d30s -s post.lua http://localhost:8080/api/v1/wallet
```

### Expected Results at 1000 RPS
- Requests/sec: 950-1050
- Latency p50: 10-20ms
- Latency p95: 30-50ms
- Latency p99: 50-100ms
- Failed requests: 0

---

## Troubleshooting

### Issue: Connection refused on port 8080
**Solution**: 
- Check if application is running: `docker-compose ps`
- Check logs: `docker-compose logs wallet-api`
- Wait 40 seconds after startup for full initialization

### Issue: Database connection failed
**Solution**:
- Ensure PostgreSQL is running
- Check database credentials in application.yml
- Verify port 5432 is open
- Check Docker network: `docker network ls`

### Issue: Port already in use
**Solution**:
```bash
# Find process using port 8080
lsof -i :8080  # Linux/Mac
netstat -ano | findstr :8080  # Windows

# Kill process or change port in docker-compose.yml
```

### Issue: OutOfMemory errors
**Solution**:
- Increase heap size in Dockerfile or docker-compose.yml
- Add JVM args: `-Xmx512m -Xms256m`

---

## Configuration

All settings can be configured via environment variables:

```bash
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=wallet_db
DB_USER=wallet_user
DB_PASSWORD=wallet_password

# Connection Pool
DB_POOL_SIZE=20
DB_MIN_IDLE=5

# Server
SERVER_PORT=8080

# Logging
LOG_LEVEL=INFO
```

Pass these to docker-compose:
```bash
DB_POOL_SIZE=30 docker-compose up -d
```

Or create a `.env` file:
```
DB_POOL_SIZE=30
DB_MIN_IDLE=10
SERVER_PORT=8080
```

---

## Project Structure

```
wallet-api/
├── src/
│   ├── main/
│   │   ├── java/com/wallet/
│   │   │   ├── WalletApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   └── WalletController.java
│   │   │   ├── dto/
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── WalletBalanceResponse.java
│   │   │   │   ├── WalletOperationRequest.java
│   │   │   │   └── WalletOperationResponse.java
│   │   │   ├── entity/
│   │   │   │   ├── Transaction.java
│   │   │   │   └── Wallet.java
│   │   │   ├── exception/
│   │   │   │   ├── InsufficientFundsException.java
│   │   │   │   ├── WalletException.java
│   │   │   │   └── WalletNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   ├── TransactionRepository.java
│   │   │   │   └── WalletRepository.java
│   │   │   └── service/
│   │   │       └── WalletService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-test.yml
│   │       └── db/changelog/
│   │           └── db.changelog-master.xml
│   └── test/
│       └── java/com/wallet/
│           ├── controller/
│           │   └── WalletControllerIntegrationTest.java
│           └── service/
│               └── WalletServiceTest.java
├── pom.xml
├── Dockerfile
└── docker-compose.yml
```

---

## Next Steps

1. **Test the API**: Run test-api.ps1 or test-api.sh
2. **Check logs**: `docker-compose logs -f`
3. **Monitor database**: Connect to PostgreSQL on localhost:5432
4. **Performance test**: Run load test with 1000 RPS
5. **Deploy**: Push to production using Docker image

---

## Support & Documentation

- API Documentation: See TEST_DEMONSTRATION.md
- Source Code: All code is in src/ directory
- Configuration: Check application.yml
- Database Schema: See Liquibase migrations in src/main/resources/db/changelog/

