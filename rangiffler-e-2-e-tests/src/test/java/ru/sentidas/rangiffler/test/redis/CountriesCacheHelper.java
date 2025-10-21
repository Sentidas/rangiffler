package ru.sentidas.rangiffler.test.redis;

import ru.sentidas.rangiffler.data.redis.RedisTestClient;
import ru.sentidas.rangiffler.data.redis.DigestPicker;
import ru.sentidas.rangiffler.data.repository.CountryDigestRepository;

import java.util.Map;

/**
 * Хелпер для теста кэширования списка стран.
 * Прячет технику: взаимодействие с Redis и чтение счётчика SQL по нужному digest.
 *
 * Контракт:
 *  - redisDeleteKey(key): удалить конкретный ключ в Redis.
 *  - redisKeyExists(key): проверить существование конкретного ключа в Redis.
 *  - getQueryCountForAllCountries(): вернуть COUNT_STAR для запроса allCountries().
 *
 * Особенность:
 *  digest для allCountries() определяется лениво:
 *   - при первом вызове метода до прогрева кэша возвращаем 0 и запоминаем «снимок ДО»;
 *   - после первого вызова сервиса на следующем обращении вычисляем digest (по разнице снимков)
 *     и с этого момента возвращаем точный COUNT_STAR по найденному digest.
 */
public class CountriesCacheHelper implements AutoCloseable {

    private final RedisTestClient redis;
    private final CountryDigestRepository repository;

    // Кэшируемый digest для allCountries(); после определения используем его напрямую.
    private String allCountriesDigest;

    // Последний зафиксированный снимок performance_schema (для определения digest через delta).
    private Map<String, CountryDigestRepository.Row> lastSnapshot;

    public CountriesCacheHelper() {
        this.redis = new RedisTestClient();
        this.repository = new CountryDigestRepository();
    }

    /**
     * Удалить конкретный ключ из Redis.
     * В реализации RedisTestClient есть удаление по шаблону — используем точечный шаблон = сам ключ.
     */
    public void redisDeleteKey(String key) {
        // Удаляем именно этот ключ (скан с match=key, безопасно и просто).
        redis.deleteByPattern(key);
    }

    /**
     * Проверить точечное существование ключа в Redis.
     */
    public boolean redisKeyExists(String key) {
        // Проверяем наличие хотя бы одного ключа, совпадающего с key.
        return redis.existsAnyByPattern(key);
    }

    /**
     * Вернуть текущее значение COUNT_STAR для запроса allCountries().
     *
     * Поведение до определения digest:
     *  - Если digest ещё не известен и это первый вызов — сохраняем «снимок ДО» и возвращаем 0.
     *  - Если digest ещё не известен и это не первый вызов — пробуем вычислить digest по разнице
     *    между предыдущим снимком и текущим; если получилось — начинаем возвращать точные значения.
     *
     * После определения digest:
     *  - Возвращаем repo.countByDigest(allCountriesDigest).
     */
    public long getQueryCountForAllCountries() {
        // Если digest уже известен — возвращаем точное значение счётчика.
        if (allCountriesDigest != null) {
            return repository.countByDigest(allCountriesDigest);
        }

        // Снимаем текущий снимок performance_schema.
        Map<String, CountryDigestRepository.Row> current = repository.snapshot();

        if (lastSnapshot == null) {
            // Первый вызов ДО прогрева: фиксируем «снимок ДО» и возвращаем 0 как базовую точку.
            lastSnapshot = current;
            return 0L;
        }

        // Пробуем определить digest по разнице «после» - «до».
        allCountriesDigest = DigestPicker
                .pickAllCountriesDigest(lastSnapshot, current)
                .orElse(null);

        // Обновляем «последний снимок» для последовательных вызовов.
        lastSnapshot = current;

        if (allCountriesDigest == null) {
            // Digest пока не удалось вычислить (например, ещё не было вызова метода сервиса).
            // Возвращаем 0 как «неизвестно/без роста».
            return 0L;
        }

        // Digest найден — возвращаем точный счётчик.
        return repository.countByDigest(allCountriesDigest);
    }

    @Override
    public void close() {
        try {
            redis.close();
        } catch (Exception ignore) {
            // Ничего: тесты не должны падать из-за закрытия клиента.
        }
    }
}
