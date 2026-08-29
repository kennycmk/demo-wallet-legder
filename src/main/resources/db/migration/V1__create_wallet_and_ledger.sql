CREATE TABLE users (
    id             UUID PRIMARY KEY,
    email          VARCHAR(255) NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE wallets (
    user_id     UUID PRIMARY KEY REFERENCES users (id),
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0 CHECK (balance >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users (id),
    entry_type        VARCHAR(16) NOT NULL CHECK (entry_type IN ('CREDIT', 'DEBIT')),
    amount            NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    balance_after     NUMERIC(19, 4) NOT NULL CHECK (balance_after >= 0),
    reason            VARCHAR(64) NOT NULL,
    idempotency_key   VARCHAR(128) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_transactions_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_transactions_user_created_at ON transactions (user_id, created_at DESC);
