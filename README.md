# Wallet Ledger Backend Service

Java 21 · Spring Boot 3.5 · PostgreSQL 16 · Flyway · OpenAPI

## How to run

### Local

Docker runs PostgreSQL. The app runs on the host (DevTools restarts on save).

```bash
docker compose up -d
./mvnw spring-boot:run
```

Windows: `mvnw.cmd spring-boot:run`

App: http://localhost:8080  
Swagger: http://localhost:8080/swagger-ui.html  
Health: http://localhost:8080/actuator/health

If Flyway fails on an older local schema: `docker compose down -v` then `docker compose up -d`.

### Production (Docker)

```bash
cp .env.example .env
docker compose -f docker-compose.prod.yml --env-file .env up --build
```

### Tests

Requires Docker (Testcontainers starts PostgreSQL 16).

```bash
./mvnw test
```

Windows: `mvnw.cmd test`

### API

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/v1/auth/register` | Creates user + empty wallet. |
| POST | `/api/v1/auth/login` | Returns JWT. |
| POST | `/api/v1/wallets/me/purchases` | JWT + `Idempotency-Key`. `{ "amount" }`. Creates **PENDING**. Does not credit yet. |
| POST | `/api/v1/webhooks/payments` | No JWT (mock gateway). `{ "purchaseId", "event": "PAYMENT_SUCCEEDED" }` credits the wallet. |
| POST | `/api/v1/wallets/me/transfers` | JWT + `Idempotency-Key`. Debit self, credit recipient. 422 if insufficient funds. |
| GET | `/api/v1/wallets/me` | Current balance. |
| GET | `/api/v1/wallets/me/transactions?page=0&size=20` | Newest first. |

## Design decisions

**Ledger model:** each user has one `wallets` row (`balance` is the current amount) and an append-only `transactions` table (every credit/debit, with type, amount, `balance_after`, reason). Register creates both the user and an empty wallet.

**Money movement:** public APIs do not let a player pick a raw credit/debit. Money in is a **purchase** (pending) completed by a **payment webhook**. Money out is a **transfer**. `WalletService.credit` / `debit` stay internal so the ledger cannot be driven as a free-form cash machine.

**Amounts:** `NUMERIC(19,4)` / `BigDecimal`. Auth is JWT; wallet APIs operate on the logged-in user.

**No optimistic locking:** the wallet row has no `@Version`. Concurrent money movement uses a row lock instead (see below). Optimistic locking would retry on conflict; under debit pressure that is extra work and still needs a final balance check.

## Concurrency & idempotency

**Concurrency:** `SELECT … FOR UPDATE` on the wallet row. Balance update and transaction insert run in the same DB transaction. Concurrent debits queue on that lock, so two threads cannot both pass an insufficient-funds check against the same snapshot. Other users are not blocked. A rejected debit writes nothing (no partial row). Transfers lock both wallets in `user_id` order to avoid deadlock.

**Idempotency:** mutating money APIs require `Idempotency-Key` (unique on `transactions` and `purchases`). The service looks up the key first, applies only if missing, and treats a unique-constraint race as “return the original row” via `TransactionTemplate` (catching `DataIntegrityViolationException` inside `@Transactional` would mark the transaction rollback-only). A duplicate request returns the original result and does not change the balance.

## Testing approach

Automated tests are **integration tests** against real PostgreSQL (Testcontainers), not mocked unit tests. Money correctness depends on row locks, unique constraints, and transaction boundaries — those do not show up in mocked repository tests.

- **API (`WalletApiTest`):** register/login, pending purchase then webhook credit, transfer, insufficient funds (balance unchanged, 422), missing JWT (401), validation, pagination (newest first), repeated `Idempotency-Key` (no extra ledger rows).
- **Concurrent debit (`WalletConcurrencyTest.concurrentDebitsNeverOverdraw`):** seed 100, 20 parallel debits of 10. Exactly 10 succeed, 10 are rejected, final balance is 0, 10 debit rows + the seed credit.
- **Concurrent idempotency:** 10 parallel credits with the same key. One transaction row, balance credited once.

## Assumptions & limitations

- Mock payment webhook is unsigned (fine for the assignment; a real gateway would HMAC the body).
- Idempotency keys are global, not scoped per user.
- Failed/rejected operations are not written to `transactions`.
- Optional extras (refunds, reservations, bulk rewards, Redis, domain events) are not implemented.
- `credit`/`debit` are callable from tests/services; they are not public HTTP endpoints.