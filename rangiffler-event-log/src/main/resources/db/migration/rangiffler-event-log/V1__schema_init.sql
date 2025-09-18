CREATE TABLE IF NOT EXISTS events (
    event_id BINARY (16) NOT NULL,
    event_type     VARCHAR(64)  NOT NULL,
    user_id BINARY (16) NOT NULL,
    photo_id BINARY (16) NULL,
    target_user_id BINARY (16) NULL,
    source_service VARCHAR(32)  NOT NULL,
    occurred_at    DATETIME(6)  NOT NULL,
    received_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    trace_id       VARCHAR(128) NULL,
    payload        JSON         NOT NULL,

    topic          VARCHAR(255) NULL,
    partition_id   INT          NULL,
    offset_value   BIGINT       NULL,
    primary key (event_id))