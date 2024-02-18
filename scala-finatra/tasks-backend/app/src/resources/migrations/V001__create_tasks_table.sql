CREATE TABLE tasks(
    userid VARCHAR(255) NOT NULL,
    id VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    create_time TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (userid,id)
);

CREATE INDEX tasks_user_id_idx ON tasks (userid);