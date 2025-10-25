
package ru.sentidas.rangiffler.controller.batch;

import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.error.domain.CountryNotFoundException;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.service.api.GrpcGeoClient;

import java.util.*;

/**
 * Батч-резолвер country для Photo: нормализуем коды, одним bulk-запросом тянем Country
 * и сопоставляем всем Photo; при отсутствии соответствия — исключение (строгий режим).
 */
@Controller
@PreAuthorize("isAuthenticated()")
public class PhotoCountryBatchResolver {

    private final GrpcGeoClient grpcGeoClient;

    public PhotoCountryBatchResolver(GrpcGeoClient grpcGeoClient) {
        this.grpcGeoClient = grpcGeoClient;
    }

    @BatchMapping(typeName = "Photo", field = "country")
    public Map<Photo, Country> resolveCountry(List<Photo> photos) {
        // 1) Собираем уникальные коды
        Set<String> codes = new LinkedHashSet<>(photos.size());
        for (Photo p : photos) {
            String code = p.countryCode();
            if (code != null && !code.isBlank()) {
                codes.add(code.trim().toLowerCase(Locale.ROOT)); // <-- БЫЛО toUpperCase, стало toLowerCase
            }
        }

        // 2) code -> Country из geo
        Map<String, Country> byCode = new HashMap<>();
        if (!codes.isEmpty()) {
            List<Country> countries = grpcGeoClient.getByCodes(List.copyOf(codes));
            for (Country c : countries) {
                if (c != null && c.code() != null) {
                    byCode.put(c.code().trim().toLowerCase(Locale.ROOT), c); // <-- нормализуем ключ
                }
            }
        }

        // 3) Формируем результат для каждого Photo;
        Map<Photo, Country> result = new LinkedHashMap<>(photos.size());
        List<String> missing = new ArrayList<>();

        for (Photo p : photos) {
            String raw = p.countryCode();
            String norm = (raw == null ? null : raw.trim().toLowerCase(Locale.ROOT)); // <-- ищем по lowerCase
            Country c = (norm != null && !norm.isBlank()) ? byCode.get(norm) : null;

            if (c == null) {
                missing.add("photo=" + p.id() + ", code=" + raw);
            } else {
                result.put(p, c); // ключ — тот же инстанс p
            }
        }

        if (!missing.isEmpty()) {
            throw new CountryNotFoundException(
                    "Country not resolved for: " + String.join("; ", missing)
            );
        }
        return result;
    }
}
