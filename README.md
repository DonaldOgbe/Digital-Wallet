# 💳 Digital Wallet

A microservices-based digital wallet application that enables secure user registration, wallet creation, peer-to-peer (P2P) transfers, and account management. The system is built with scalability, modularity, and service-to-service communication in mind.

---

## 🚀 Functional Requirements

* **User Service**

    * User registration & login with JWT authentication
    * Profile management (view/update user details)
    * Event publishing on user registration

* **Wallet Service**

    * Automatic wallet creation when a user registers
    * Manage multiple accounts per wallet
    * Account number generation & validation
    * PIN management (set, update, validate)
    * Balance checking
    * Event publishing on wallet creation

* **Transaction Service**

    * P2P transfers between accounts
    * Reserve, transfer, and release funds
    * Transaction history & tracking
    * Integration with Wallet Service via Wallet Client

---

## 📐 Non-Functional Requirements

* **Security**: JWT authentication, PIN validation for sensitive operations
* **Reliability**: Event-driven architecture with RabbitMQ for decoupled communication
* **Scalability**: Services deployed as independent containers via Docker & Docker Compose
* **Maintainability**: Clear service boundaries and TDD-driven implementation
* **Testability**: Unit and integration tests across services

---

## 🛠️ Technologies

* **Backend**: Java, Spring Boot, Spring Security
* **Database**: PostgreSQL
* **Messaging**: RabbitMQ (event-driven communication)
* **Caching**: Redis (planned for refresh tokens)
* **Testing**: JUnit, Mockito, MockWebServer
* **Deployment**: Docker, Docker Compose
* **External APIs**: Flutterwave (sandbox for transfers & card funding)

---

## ✨ Extra Features (Planned / Future Work)

* 🔄 **Refresh Tokens with Redis caching**
* 🌐 **OAuth with Google for social login**
* 🐇 **RabbitMQ integration for wallet events**
* 🔑 **Update Password endpoint**
* 💰 **Get Balance endpoint**
* 🌍 **External transfers via Flutterwave sandbox API**
* 💳 **Funding wallets using Flutterwave card API**

---

## 📦 System Design Overview

The system follows a **microservices architecture**, with each services sharing database and exposing REST APIs.

* Services communicate asynchronously via **RabbitMQ** for critical events (`UserRegistered`, `WalletCreated`).
* Synchronous calls between services (e.g., Transaction → Wallet) are handled via **REST clients**.
* Each service is independently deployable and can scale horizontally.
