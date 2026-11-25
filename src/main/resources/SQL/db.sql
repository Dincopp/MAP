CREATE TABLE IF NOT EXISTS users (
                       id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                       first_name VARCHAR NOT NULL,
                       last_name VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS friendships (
                             id1 BIGINT NOT NULL,
                             id2 BIGINT NOT NULL,
                             friends_from TIMESTAMP NOT NULL,
                             PRIMARY KEY (id1, id2),
                             CONSTRAINT fk_user1 FOREIGN KEY (id1) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_user2 FOREIGN KEY (id2) REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT chk_id_order CHECK (id1 < id2)
);

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

SELECT * FROM users;
SELECT * FROM friendships;
SELECT * FROM friend_requests;
