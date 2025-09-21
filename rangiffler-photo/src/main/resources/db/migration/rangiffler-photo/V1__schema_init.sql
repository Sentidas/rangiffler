CREATE TABLE IF NOT EXISTS `photo`
(
    `id`           BINARY(16)             NOT NULL,
    `user_id`      BINARY(16)             NOT NULL,
    `country_code` CHAR(2)                NOT NULL,
    `description`  TEXT                   NULL,
    `storage`      ENUM ('OBJECT','BLOB') NOT NULL DEFAULT 'OBJECT',
    `photo`        LONGBLOB               NULL, -- для режима BLOB
    `photo_url`    VARCHAR(512)           NULL, -- для режима OBJECT (ключ MinIO)
    `created_date` DATETIME               NOT NULL,
    PRIMARY KEY (`id`),
    KEY `idx_photo_user_created` (`user_id`, `created_date`),
    KEY `idx_photo_created` (`created_date`)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS `photo_like`
(
    `photo_id`     BINARY(16) NOT NULL,
    `user_id`      BINARY(16) NOT NULL,
    `created_date` DATETIME   NOT NULL,
    PRIMARY KEY (`photo_id`, `user_id`)
) ENGINE = InnoDB;
