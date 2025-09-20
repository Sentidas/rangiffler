-- Индексы под типовые запросы
CREATE INDEX ix_stat_user ON statistic (user_id);
CREATE INDEX ix_stat_country ON statistic (country_id);
CREATE UNIQUE INDEX ux_country_code ON country (code);

-- Гарантия единственности связки (user_id, country_id)
ALTER TABLE statistic
    ADD CONSTRAINT uq_stat_user_country UNIQUE (user_id, country_id);