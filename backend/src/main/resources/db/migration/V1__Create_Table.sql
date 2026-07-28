CREATE TABLE if NOT EXISTS transaction (
    id INTEGER,
    value REAL,
    vendor VARCHAR(255),
    time TIMESTAMP,
    PRIMARY KEY (id)
);