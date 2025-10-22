-- Владелец
-- 11111111-1111-1111-1111-111111111111

-- Фото под удаление (OBJECT)
-- 22222222-2222-2222-2222-222222222222
INSERT INTO photo (
    id,          -- UUID
    user_id,     -- UUID владельца
    country_code,
    description,
    created_date,
    storage,
    photo,
    photo_url
)
VALUES (
           CAST('22222222-2222-2222-2222-222222222222' AS UUID),
           CAST('11111111-1111-1111-1111-111111111111' AS UUID),
           'FR',
           'to be deleted',
           TIMESTAMP '2025-03-04 05:06:07',
           'OBJECT',
           NULL,
           'minio/photos/1111/2222.jpg'
       );

-- Фото под обновление описания (OBJECT)
-- 44444444-4444-4444-4444-444444444444
INSERT INTO photo (
    id, user_id, country_code, description, created_date, storage, photo, photo_url
)
VALUES (
           CAST('44444444-4444-4444-4444-444444444444' AS UUID),
           CAST('11111111-1111-1111-1111-111111111111' AS UUID),
           'FR',
           'old desc',
           TIMESTAMP '2025-03-05 06:07:08',
           'OBJECT',
           NULL,
           'minio/photos/1111/4444.jpg'
       );

-- Опционально: лайк на удаляемое фото
INSERT INTO photo_like (user_id, photo_id, created_date)
VALUES (
           CAST('33333333-3333-3333-3333-333333333333' AS UUID),
           CAST('22222222-2222-2222-2222-222222222222' AS UUID),
           CURRENT_TIMESTAMP
       );
