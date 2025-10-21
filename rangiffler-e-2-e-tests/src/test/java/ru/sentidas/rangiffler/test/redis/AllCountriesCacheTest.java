package ru.sentidas.rangiffler.test.redis;

import com.google.protobuf.Empty;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.CountriesResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.RedisCacheTest;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.*;
//@Tag("redis-postcheck")
@RedisCacheTest
@DisplayName("Кэширование списка стран")
class AllCountriesCacheTest extends BaseTest {

    // Фиксированный ключ кэша для allCountries()
    private static final String COUNTRIES_CACHE_KEY = "rangiffler-geo:countries::all";

    // Минимальный вспомогательный объект: прячет детали Redis и счётчика SQL
    private final CountriesCacheHelper helper = new CountriesCacheHelper();

    @Test
    @DisplayName("Очистка → 1-й вызов даёт SQL-хит и создаёт ключ → 2-й вызов кэш-хит и ключ сохраняется")
    void warmThenCacheHit() throws InterruptedException {
        // --- Подготовка окружения ---
        // Удаляем именно наш ключ кэша, стартуем с пустого состояния.
        helper.redisDeleteKey(COUNTRIES_CACHE_KEY);
        Thread.sleep(5000);

        // Проверяем, что ключа кэша нет перед началом теста.
        assertFalse(helper.redisKeyExists(COUNTRIES_CACHE_KEY),
                "Перед тестом ключ кэша должен отсутствовать: " + COUNTRIES_CACHE_KEY);

        // --- Первый вызов сервиса ---
        // Фиксируем текущее значение счётчика SQL до вызова.
        long sqlCountBeforeFirstCall = helper.getQueryCountForAllCountries();

        // Вызываем метод получения всех стран.
        CountriesResponse firstResponse = geoBlockingStub.allCountries(Empty.getDefaultInstance());

        // Ответ должен быть не пустым.
        assertTrue(firstResponse.getCountriesCount() > 0, "Ответ не пустой");

        // После первого вызова счётчик SQL должен вырасти — ходили в БД.
        long sqlCountAfterFirstCall = helper.getQueryCountForAllCountries();
        assertTrue(sqlCountAfterFirstCall > sqlCountBeforeFirstCall, "На первом вызове ожидаем SQL-хит");

        // После первого вызова должен появиться ровно этот ключ кэша.
        assertTrue(helper.redisKeyExists(COUNTRIES_CACHE_KEY),
                "После 1-го вызова должен появиться ключ кэша: " + COUNTRIES_CACHE_KEY);

        // --- Второй вызов сервиса ---
        // Делаем повторный вызов того же метода.
        CountriesResponse secondResponse = geoBlockingStub.allCountries(Empty.getDefaultInstance());

        // Размер ответа должен совпадать — берём из кэша.
        assertEquals(firstResponse.getCountriesCount(), secondResponse.getCountriesCount(),
                "Размер ответа должен совпадать между вызовами (кэш стабилен)");

        // После второго вызова счётчик SQL не должен измениться — кэш-хит.
        long sqlCountAfterSecondCall = helper.getQueryCountForAllCountries();
        assertEquals(sqlCountAfterFirstCall, sqlCountAfterSecondCall,
                "На втором вызове SQL не должен выполняться (кэш-хит)");

        // Ключ кэша должен сохраняться и после второго вызова.
        assertTrue(helper.redisKeyExists(COUNTRIES_CACHE_KEY),
                "Ключ кэша должен сохраняться: " + COUNTRIES_CACHE_KEY);
    }
}