create table if not exists `photo`
(
    id                      binary(16) unique not null,
    user_id                 binary(16)        not null,
    country_code            char(2)        not null,
    `description`           text,
    photo                   longblob,
    created_date            datetime not null,
    primary key (id)
);


create table if not exists `photo_like`
(
    photo_id                 binary(16)        not null,
    user_id                  binary(16)        not null,
    created_date            datetime not null,
    primary key (photo_id, user_id)
);
