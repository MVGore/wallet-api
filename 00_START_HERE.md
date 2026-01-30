This project implements a Wallet API in Java Spring Boot with PostgreSQL and Liquibase migrations, containerized using Docker.

It supports deposit, withdrawal, and balance retrieval with concurrency-safe operations.

QUICK_START / STARTUP_GUIDE
Prerequisites

Docker & Docker Compose installed

Maven installed

Postman (or any API client) for testing

Steps to Run

Clone the repository:

git clone <repository_url>
cd wallet-api


Build and start containers:

docker-compose up --build


Verify services are running:

docker ps


Access API endpoints on http://localhost:8080/api/v1