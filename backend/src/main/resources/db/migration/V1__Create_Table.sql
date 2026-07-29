CREATE TABLE IF NOT EXISTS transactions.counterparty
(
    id   SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS users
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL,
    email    VARCHAR(100)        NOT NULL
);
CREATE TABLE IF NOT EXISTS transactions.transactions
(
    id              BIGSERIAL PRIMARY KEY,
    amount          NUMERIC(12, 2) NOT NULL,
    counterparty_id INT            NOT NULL REFERENCES counterparty (id),
    time            TIMESTAMPTZ    NOT NULL DEFAULT now()
);
