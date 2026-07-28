CREATE TABLE if NOT EXISTS transactions.transactions (
    id SERIAL PRIMARY KEY,
    value REAL,
    vendor VARCHAR(255),
    time TIMESTAMP
);