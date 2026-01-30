# Wallet API Test Script - PowerShell Version
# This script demonstrates all API endpoints and expected outputs

param(
    [string]$BaseUrl = "http://localhost:8080/api/v1"
)

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Wallet API Integration Tests" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

$walletId = ""

# Test 1: Create a wallet
Write-Host "TEST 1: Creating a new wallet..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
    $walletId = $response.walletId
    Write-Host "Created Wallet ID: $walletId" -ForegroundColor Green
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 2: Get wallet balance (should be 0)
Write-Host "TEST 2: Getting initial wallet balance..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets/$walletId" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 3: Deposit money
Write-Host "TEST 3: Depositing 5000 units..." -ForegroundColor Yellow
try {
    $body = @{
        walletId = $walletId
        operationType = "DEPOSIT"
        amount = 5000
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallet" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 4: Check balance after deposit
Write-Host "TEST 4: Checking balance after deposit..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets/$walletId" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 5: Withdraw money
Write-Host "TEST 5: Withdrawing 1500 units..." -ForegroundColor Yellow
try {
    $body = @{
        walletId = $walletId
        operationType = "WITHDRAW"
        amount = 1500
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallet" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 6: Check balance after withdrawal
Write-Host "TEST 6: Checking balance after withdrawal..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets/$walletId" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 7: Try to withdraw more than balance (should fail)
Write-Host "TEST 7: Attempting to withdraw 5000 (should fail)..." -ForegroundColor Yellow
try {
    $body = @{
        walletId = $walletId
        operationType = "WITHDRAW"
        amount = 5000
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallet" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Expected Error (Insufficient Funds):" -ForegroundColor Yellow
    Write-Host $_.Exception.Response.StatusCode -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message -ForegroundColor Red
}
Write-Host ""

# Test 8: Try to get non-existent wallet
Write-Host "TEST 8: Attempting to get non-existent wallet..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets/00000000-0000-0000-0000-000000000000" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Expected Error (Not Found):" -ForegroundColor Yellow
    Write-Host $_.Exception.Response.StatusCode -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message -ForegroundColor Red
}
Write-Host ""

# Test 9: Get all wallets
Write-Host "TEST 9: Getting all wallets..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallets" `
        -Method GET `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Error: $_" -ForegroundColor Red
}
Write-Host ""

# Test 10: Invalid JSON
Write-Host "TEST 10: Sending invalid JSON..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/wallet" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body "{ invalid json }" `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response.Content
} catch {
    Write-Host "Expected Error (Invalid JSON):" -ForegroundColor Yellow
    Write-Host $_.Exception.Response.StatusCode -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message -ForegroundColor Red
}
Write-Host ""

# Test 11: Missing required fields
Write-Host "TEST 11: Sending request with missing 'amount' field..." -ForegroundColor Yellow
try {
    $body = @{
        walletId = $walletId
        operationType = "DEPOSIT"
    } | ConvertTo-Json
    
    $response = Invoke-RestMethod -Uri "$BaseUrl/wallet" `
        -Method POST `
        -Headers @{"Content-Type"="application/json"} `
        -Body $body `
        -ErrorAction Stop
    
    Write-Host "Response:" -ForegroundColor Green
    $response | ConvertTo-Json
} catch {
    Write-Host "Expected Error (Validation):" -ForegroundColor Yellow
    Write-Host $_.Exception.Response.StatusCode -ForegroundColor Red
    Write-Host $_.ErrorDetails.Message -ForegroundColor Red
}
Write-Host ""

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "All tests completed!" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
