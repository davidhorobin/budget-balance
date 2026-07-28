CREATE TABLE if NOT EXISTS transactions.transactions (
    id SERIAL PRIMARY KEY,
    amount NUMERIC(12, 2) NOT NULL,
    counterparty VARCHAR(100) NOT NULL,
    time TIMESTAMPTZ NOT NULL DEFAULT now()
);