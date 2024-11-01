CREATE TABLE IF NOT EXISTS merchant (
    id VARCHAR(50) PRIMARY KEY,
    last_update_time TIMESTAMP,
    creation_time TIMESTAMP,
    version BIGINT,
    products TEXT
);

CREATE TABLE IF NOT EXISTS event_to_dispatch (
    id SERIAL PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    content TEXT
);