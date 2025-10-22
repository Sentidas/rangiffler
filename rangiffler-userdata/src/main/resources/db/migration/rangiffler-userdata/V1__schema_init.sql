create table if not exists `user`
(
    id           binary(16) unique  not null default (UUID_TO_BIN(UUID(), true)),
    username     varchar(50) unique not null,
    firstname    varchar(255),
    surname      varchar(255),
    country_code char(2)            not null,
    storage       ENUM('OBJECT','BLOB') not null, -- текущий режим хранения аватара
    object_key    varchar(512) null,     -- для OBJECT: avatars/{userId}/{uuid}.ext
    avatar        longblob     null,     -- для BLOB: байты оригинала
    mime          varchar(64)  null,     -- image/png, image/jpeg, image/gif, ...
    avatar_small  longblob     null,     -- миниатюра (PNG)
    primary key (id)
);

create table if not exists `friendship`
(
    requester_id binary(16)  not null,
    addressee_id binary(16)  not null,
    created_date datetime    not null,
    status       varchar(50) not null,   -- PENDING / ACCEPTED
    primary key (requester_id, addressee_id),

    -- Запрет дружить с самим собой
    constraint friend_are_distinct_ck check (requester_id <> addressee_id),
    constraint fk_requester_id foreign key (requester_id) references `user` (id),
    constraint fk_addressee_id foreign key (addressee_id) references `user` (id)
) ENGINE=InnoDB;

-- [IDX #1] Ускоряет выборки исходящих и «моих друзей»:
-- WHERE requester_id = ? AND status IN ('PENDING','ACCEPTED')
-- Нужен для: friends(...), outcomeInvitations(...), findUsersWithBiStatus(...).
CREATE INDEX friendship_req_status_idx
    ON friendship (requester_id, status);

-- [IDX #2] Ускоряет выборки входящих инвайтов:
-- WHERE addressee_id = ? AND status = 'PENDING'
-- Нужен для: incomeInvitations(...).
CREATE INDEX friendship_add_status_idx
    ON friendship (addressee_id, status);

