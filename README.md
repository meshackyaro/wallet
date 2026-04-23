# Koins Wallet API

A professional-grade fintech backend application for wallet management, featuring secure transactions, real-time notifications via RabbitMQ, and seamless payment integration with Paystack.

---

## Getting Started

### Prerequisites

Ensure you have the following installed:
- **Java 21** (LTS)
- **Maven 3.9+**
- **Docker & Docker Compose** (Recommended for infrastructure)
- **PostgreSQL 15+**
- **RabbitMQ**

### Setup Instructions

#### 1. Clone the Repository
```bash
git clone <repository-url>
cd wallet
```

#### 2. Infrastructure Setup (Recommended)
The easiest way to start the required services (PostgreSQL & RabbitMQ) is using Docker:
```bash
docker-compose up -d postgres rabbitmq
```

#### 3. Configure Environment Variables
Create a `.env` file in the root directory with the following configurations:
```env
# Database
DB_URL=<your-database-url>
DB_USERNAME=<your-database-username>
DB_PASSWORD=<your-database-password>

# Security
JWT_SECRET=<your-jwt-secret>
JWT_EXPIRATION=<token-expiration-ms>

# Mail
MAIL_HOST=<smtp-host>
MAIL_PORT=<smtp-port>
MAIL_USERNAME=<smtp-username>
MAIL_PASSWORD=<smtp-password>

# Paystack
PAYSTACK_SECRET_KEY=<your-paystack-secret-key>
```

#### 4. Run the Application
```bash
./mvnw spring-boot:run
```
The server will start at `http://localhost:8080`.

---

## Database Configuration

The application uses **PostgreSQL** as its primary data store. 

- **Database Name**: `koins_db`
- **Dialect**: `PostgreSQLDialect`
- **Hibernate Strategy**: `update` (auto-creates tables on startup)

To manually set up the database:
1. Create the database.
2. Update the `DB_URL` in your `.env` or `application.yml`.

---

## Environment Variables

| Variable | Description |
| :--- | :--- |
| `DB_URL` | PostgreSQL Connection URL |
| `DB_USERNAME` | Database Username |
| `DB_PASSWORD` | Database Password |
| `JWT_SECRET` | Secret key for JWT signing |
| `JWT_EXPIRATION` | JWT Token validity (ms) |
| `MAIL_HOST` | SMTP Server Host |
| `MAIL_PORT` | SMTP Server Port |
| `MAIL_USERNAME` | SMTP Username |
| `MAIL_PASSWORD` | SMTP App Password |
| `PAYSTACK_SECRET_KEY` | Paystack Secret API Key |

---

## API Documentation

Interactive API documentation is provided via Swagger UI. Once the application is running, you can access it at:

[Swagger UI](http://localhost:8080/swagger-ui.html)

You can also download the raw OpenAPI specification:
- **JSON**: `http://localhost:8080/v3/api-docs`

---

## Tech Stack

- **Framework**: Spring Boot 3.4.3
- **Language**: Java 21
- **Security**: Spring Security & JWT
- **Messaging**: RabbitMQ
- **Database**: PostgreSQL / Spring Data JPA
- **API Docs**: SpringDoc OpenAPI
- **Integrations**: Paystack API
