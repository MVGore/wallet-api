# 🚀 Wallet API — Secure Digital Wallet Backend

A **production-ready Spring Boot backend** for a digital wallet system supporting authentication, transactions, admin controls, and secure JWT-based authorization.

---

## 🔥 Features

* 🔐 **JWT Authentication & Authorization**
* 👤 User Registration & Login
* 💰 Wallet Management (Credit / Debit)
* 🔄 Money Transfer Between Users
* 📊 Transaction History (Search, Filter, Pagination)
* 🛡️ Admin Controls (Freeze/Unfreeze Wallets, Manage Users)
* 🚫 Token Revocation (Logout Security with Token Versioning)
* ⚡ Role-Based Access Control (ADMIN / USER)

---

## 🧠 Architecture

```
Controller → Service → Repository → Database
```

* **Controller Layer** → Handles API requests
* **Service Layer** → Business logic
* **Repository Layer** → Database interaction (JPA)
* **Security Layer** → JWT + Spring Security

---

## 🔐 Security

* Stateless authentication using JWT
* Role-based access:

    * `ADMIN` → Full control
    * `USER` → Wallet operations
* Token versioning ensures:

    * Logout invalidates previous tokens
* Custom JWT filter for request validation

---

## ⚙️ Tech Stack

* Java 17
* Spring Boot
* Spring Security
* Spring Data JPA
* MySQL
* JWT (jjwt)
* Maven
* Docker

---

## 📂 Project Structure

```
src/
 ├── controller/
 ├── service/
 ├── repository/
 ├── entity/
 ├── dto/
 ├── security/
 └── configuration/
```

---

## ⚡ Setup Instructions

### 1️⃣ Clone the repo

```
git clone https://github.com/<your-username>/wallet-api.git
cd wallet-api
```

---

### 2️⃣ Configure Environment Variables

Create a `.env` file:

```
DB_URL=jdbc:mysql://localhost:3306/wallet_api
DB_USERNAME=username
DB_PASSWORD=yourpassword

JWT_SECRET=your_super_secret_key
JWT_EXPIRATION=3600000
```

---

### 3️⃣ Run the application

```
mvn spring-boot:run
```

---

## 🐳 Run with Docker

```
docker build -t wallet-api .
docker run -p 8080:8080 \
-e DB_URL=jdbc:mysql://host.docker.internal:3306/wallet_api \
-e DB_USERNAME=username \
-e DB_PASSWORD=yourpassword \
-e JWT_SECRET=your_secret \
wallet-api
```

---

## 📡 API Endpoints

### 🔐 Auth

* `POST /api/auth/register`
* `POST /api/auth/login`
* `POST /api/auth/logout`

---

### 👤 User

* `GET /api/user/me`
* `POST /api/user/change-password`
* `POST /api/user/forgot-password`
* `POST /api/user/reset-password`

---

### 💰 Wallet

* `GET /api/wallet/balance`
* `POST /api/wallet/credit`
* `POST /api/wallet/debit`
* `POST /api/wallet/transfer`

---

### 🛡️ Admin

* Manage users
* Freeze/unfreeze wallets
* View all transactions

---

## 📸 Screenshots

![Screenshot (79).png](ScreenShots/Screenshot%20%2879%29.png)
![Screenshot (76).png](ScreenShots/Screenshot%20%2876%29.png)
![Screenshot (78).png](ScreenShots/Screenshot%20%2878%29.png)
![Screenshot (80).png](ScreenShots/Screenshot%20%2880%29.png)
![Screenshot (81).png](ScreenShots/Screenshot%20%2881%29.png)
![Screenshot (82).png](ScreenShots/Screenshot%20%2882%29.png)

---

## 🧪 Testing

```
mvn test
```

---

## 📌 Why This Project Matters

This project demonstrates:

* Real-world backend architecture
* Secure authentication design
* Scalable service-layer logic
* Production-level coding practices

---

## 👨‍💻 Author

**Mandar Gore**

---

## ⭐ Show Your Support

If you like this project, give it a ⭐ on GitHub!
