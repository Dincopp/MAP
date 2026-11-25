CREATE TABLE IF NOT EXISTS friend_requests (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    from_id BIGINT NOT NULL,
    to_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    date TIMESTAMP NOT NULL,
    CONSTRAINT fk_request_from FOREIGN KEY (from_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_request_to FOREIGN KEY (to_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_request_not_self CHECK (from_id <> to_id)
);
