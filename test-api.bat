@echo off
REM Wallet API Test Script for Windows
REM This script demonstrates all API endpoints and expected outputs

setlocal enabledelayedexpansion

set "BASE_URL=http://localhost:8080/api/v1"
set "WALLET_ID="

echo ==================================
echo Wallet API Integration Tests
echo ==================================
echo.

REM Test 1: Create a wallet
echo TEST 1: Creating a new wallet...
for /f "tokens=*" %%a in ('curl -s -X POST "!BASE_URL!/wallets" -H "Content-Type: application/json"') do (
    echo Response: %%a
    set "RESPONSE=%%a"
)
REM Extract wallet ID from response (simplified)
echo Creating wallet (check response above for wallet ID)
echo.

REM Note: For complete testing with jq parsing, use PowerShell version
echo NOTE: For full JSON parsing, run test-api.ps1 instead
echo.
