CREATE TABLE purchases (
    id                UUID PRIMARY KEY,
    user_id           UUID NOT NULL REFERENCES users (id),
    amount            NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    status            VARCHAR(16) NOT NULL CHECK (status IN ('PENDING', 'PAID', 'FAILED')),
    idempotency_key   VARCHAR(128) NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    paid_at           TIMESTAMPTZ,
    CONSTRAINT uq_purchases_idempotency UNIQUE (idempotency_key)
);

CREATE INDEX idx_purchases_user_created_at ON purchases (user_id, created_at DESC);
