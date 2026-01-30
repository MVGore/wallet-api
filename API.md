API


POST /wallet

Perform deposit or withdrawal.

Request Body (JSON):

{
  "walletId": "UUID",
  "operationType": "DEPOSIT | WITHDRAW",
  "amount": 1000
}


Response (JSON):

{
  "id": "UUID",
  "balance": 1000.00
}


Errors:

Wallet not found → 404 { "error": "Wallet not found" }

Insufficient funds → 400 { "error": "Insufficient funds" }

Invalid operation → 400 { "error": "Invalid request" }

GET /wallets/{walletId}

Retrieve wallet balance.

Response (JSON):

{
  "id": "UUID",
  "balance": 1000.00
}