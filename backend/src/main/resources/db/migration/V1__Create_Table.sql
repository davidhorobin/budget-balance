CREATE TYPE transaction_type AS ENUM ('Deposit', 'Purchase', 'Transfer', 'Withdrawal');
CREATE TABLE IF NOT EXISTS counterparties
(
    id   BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS users
(
    id       BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(60)         NOT NULL,
    email    VARCHAR(100) UNIQUE NOT NULL
);
CREATE TABLE IF NOT EXISTS bank_accounts
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    name            VARCHAR(100)   NOT NULL,
    user_id         BIGINT         NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    counterparty_id BIGINT         NOT NULL REFERENCES counterparties (id),
    balance         NUMERIC(12, 2) NOT NULL,
    currency        VARCHAR(3)     NOT NULL CHECK (currency IN ('GBP', 'USD', 'EUR')),
    CONSTRAINT uq_name_user UNIQUE (name, user_id)
);
CREATE TABLE IF NOT EXISTS refresh_tokens
(
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    hash        VARCHAR(255) UNIQUE NOT NULL,
    user_id     BIGINT              NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP           NOT NULL,
    revoked     BOOLEAN DEFAULT FALSE
);
CREATE TABLE IF NOT EXISTS transactions
(
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    bank_account_id BIGINT           NOT NULL REFERENCES bank_accounts (id),
    counterparty_id BIGINT           NOT NULL REFERENCES counterparties (id),
    amount          NUMERIC(12, 2)   NOT NULL,
    currency        VARCHAR(3)       NOT NULL CHECK (currency IN ('GBP', 'USD', 'EUR')),
    type            transaction_type NOT NULL,
    time            TIMESTAMP        NOT NULL
);
