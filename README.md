# Cafe Booking System

A multi-module restaurant/cafe booking and order management system.

## Modules

- **backend/** — Spring Boot 3.x REST API (Java 21, JWT security, Spring Data JPA, Flyway).
- **frontend/** — Angular 17+ SPA using the modern signals API (standalone components, TypeScript).
- **.devcontainer/** — VS Code Dev Container with Postgres + JDK + Node toolchain for local development.

## Features

### Functional
- Waiters authenticate with a 4-digit PIN (JWT session afterwards).
- Create orders for tables and open tabs.
- Move tabs between tables / merge two tabs.
- Remove individual orders from a tab.
- Checkout by cash, card, or online payment.
- Split checkout: one tab paid by multiple people with different methods.
- Free-text notes on every order.
- Tip field on payment.
- Manual price override requires a written reason.

### Non-functional
- PostgreSQL with Flyway migrations.
- REST API (OpenAPI-friendly DTOs).
- Angular signals for reactive state on the frontend.
- Tabs/orders are globally visible and searchable (server-side, not per device).
- Dev container for consistent local dev.

## Quick start (with Docker Compose)

```bash
docker compose up --build
```

- Backend: http://localhost:8080
- Frontend: http://localhost:4200
- Postgres: localhost:5432 (db=cafe, user=cafe, password=cafe)

### Seeded login

A demo waiter is seeded by Flyway:
- Name: `Alice`, PIN: `1234`
- Name: `Bob`, PIN: `5678`

## Quick start (Dev Container)

Open the repository in VS Code and choose **"Reopen in Container"**.
The container provides Java 21, Maven, Node 20, the Angular CLI, and a running Postgres.

## Local manual run

```bash
# 1. Start Postgres (any way you like)
docker run --rm -p 5432:5432 -e POSTGRES_DB=cafe -e POSTGRES_USER=cafe -e POSTGRES_PASSWORD=cafe postgres:16

# 2. Backend
cd backend
./mvnw spring-boot:run

# 3. Frontend (in a second terminal)
cd frontend
npm install
npm start
```

## API shape

See `backend/src/main/java/com/cafe/booking/controller/` — the controllers are the source of truth. A short tour:

| Method | Path | Purpose |
|--------|------|---------|
| POST   | `/api/auth/login` | Exchange PIN for JWT |
| GET    | `/api/tables` | List tables |
| GET    | `/api/menu` | List menu items |
| GET    | `/api/tabs` | Search/list tabs (filters: status, tableId, waiterId, q) |
| POST   | `/api/tabs` | Open a tab on a table |
| PATCH  | `/api/tabs/{id}/move` | Move tab to another table |
| POST   | `/api/tabs/{id}/merge/{otherId}` | Merge another tab into this one |
| POST   | `/api/tabs/{id}/orders` | Add an order to a tab |
| DELETE | `/api/tabs/{id}/orders/{orderId}` | Remove an order |
| PATCH  | `/api/tabs/{id}/orders/{orderId}/price` | Override price (requires `reason`) |
| POST   | `/api/tabs/{id}/payments` | Add a payment (split-friendly) |
| POST   | `/api/tabs/{id}/close` | Close the tab once fully paid |
