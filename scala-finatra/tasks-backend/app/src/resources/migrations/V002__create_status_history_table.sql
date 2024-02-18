CREATE SEQUENCE task_metadata_id_seq INCREMENT BY 1 START 1;

CREATE TABLE task_metadata(
    id BIGINT DEFAULT nextval('task_metadata_id_seq'),
    user_id VARCHAR(255) NOT NULL,
    task_id VARCHAR(255) NOT NULL,

    metadata_name VARCHAR(255) NOT NULL,
    metadata_type VARCHAR(32) NOT NULL,
    create_time TIMESTAMP DEFAULT NOW(),

    metadata_long_value BIGINT,
    metadata_double_value DOUBLE PRECISION,
    metadata_short_text_value VARCHAR(255),
    metadata_uuid_value UUID,
    metadata_boolean_value BOOL,

    PRIMARY KEY (id)
);

CREATE INDEX metadata_create_time_index ON task_metadata (create_time ASC);
CREATE INDEX metadata_user_task_index ON task_metadata (user_id,task_id);