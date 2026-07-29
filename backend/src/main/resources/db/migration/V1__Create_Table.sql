CREATE TABLE if NOT EXISTS transactions.transactions (
    id BIGSERIAL PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    counterparty VARCHAR(100) NOT NULL,
    time TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS transactions.counterparty (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL
);