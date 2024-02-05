CREATE SEQUENCE status_history_id INCREMENT BY 1 START 1;

CREATE TABLE task_status_history(
    id BIGINT DEFAULT nextval('status_history_id'),
    user_id VARCHAR(255) NOT NULL,
    task_id VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (id)
);

CREATE INDEX status_history_create_time_index ON task_status_history (create_time ASC);