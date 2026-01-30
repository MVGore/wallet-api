#!/bin/bash
# Wallet API Test Script
# This script demonstrates all API endpoints and expected outputs

BASE_URL="http://localhost:8080/api/v1"
WALLET_ID=""

echo "=================================="
echo "Wallet API Integration Tests"
echo "=================================="
echo ""

# Test 1: Create a wallet
echo "TEST 1: Creating a new wallet..."
RESPONSE=$(curl -s -X POST "$BASE_URL/wallets" \
  -H "Content-Type: application/json")
echo "Response: $RESPONSE"
WALLET_ID=$(echo $RESPONSE | grep -o '"walletId":"[^"]*' | cut -d'"' -f4)
echo "Created Wallet ID: $WALLET_ID"
echo ""

# Test 2: Get wallet balance (should be 0)
echo "TEST 2: Getting initial wallet balance..."
curl -s -X GET "$BASE_URL/wallets/$WALLET_ID" | jq .
echo ""

# Test 3: Deposit money
echo "TEST 3: Depositing 5000 units..."
curl -s -X POST "$BASE_URL/wallet" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"$WALLET_ID\",
    \"operationType\": \"DEPOSIT\",
    \"amount\": 5000
  }" | jq .
echo ""

# Test 4: Check balance after deposit
echo "TEST 4: Checking balance after deposit..."
curl -s -X GET "$BASE_URL/wallets/$WALLET_ID" | jq .
echo ""

# Test 5: Withdraw money
echo "TEST 5: Withdrawing 1500 units..."
curl -s -X POST "$BASE_URL/wallet" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"$WALLET_ID\",
    \"operationType\": \"WITHDRAW\",
    \"amount\": 1500
  }" | jq .
echo ""

# Test 6: Check balance after withdrawal
echo "TEST 6: Checking balance after withdrawal..."
curl -s -X GET "$BASE_URL/wallets/$WALLET_ID" | jq .
echo ""

# Test 7: Try to withdraw more than balance (should fail)
echo "TEST 7: Attempting to withdraw 5000 (should fail with insufficient funds)..."
curl -s -X POST "$BASE_URL/wallet" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"$WALLET_ID\",
    \"operationType\": \"WITHDRAW\",
    \"amount\": 5000
  }" | jq .
echo ""

# Test 8: Try to get non-existent wallet
echo "TEST 8: Attempting to get balance from non-existent wallet..."
curl -s -X GET "$BASE_URL/wallets/00000000-0000-0000-0000-000000000000" | jq .
echo ""

# Test 9: Get all wallets
echo "TEST 9: Getting all wallets..."
curl -s -X GET "$BASE_URL/wallets" | jq .
echo ""

# Test 10: Invalid JSON
echo "TEST 10: Sending invalid JSON (should fail)..."
curl -s -X POST "$BASE_URL/wallet" \
  -H "Content-Type: application/json" \
  -d '{ invalid json }' | jq .
echo ""

# Test 11: Missing required fields
echo "TEST 11: Sending request with missing 'amount' field..."
curl -s -X POST "$BASE_URL/wallet" \
  -H "Content-Type: application/json" \
  -d "{
    \"walletId\": \"$WALLET_ID\",
    \"operationType\": \"DEPOSIT\"
  }" | jq .
echo ""

echo "=================================="
echo "All tests completed!"
echo "=================================="
