CREATE SCHEMA if NOT EXISTS transactions;
CREATE TABLE if NOT EXISTS transaction (
    id SERIAL PRIMARY KEY,
    value REAL,
    vendor VARCHAR(255),
    time TIMESTAMP
);