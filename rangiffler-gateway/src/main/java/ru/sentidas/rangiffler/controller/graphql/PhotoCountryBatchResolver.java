
package ru.sentidas.rangiffler.controller.graphql;

import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.ggl.input.Country;
import ru.sentidas.rangiffler.service.api.GrpcGeoClient;

import java.util.*;

@Controller
public class PhotoCountryBatchResolver {

    private final GrpcGeoClient grpcGeoClient;

    public PhotoCountryBatchResolver(GrpcGeoClient grpcGeoClient) {
        this.grpcGeoClient = grpcGeoClient;
    }

    @BatchMapping(typeName = "Photo", field = "country")
    public Map<Photo, Country> country(List<Photo> photos) {
        // 1) собираем коды с текущей страницы
        List<String> codes = new ArrayList<>(photos.size());
        for (Photo p : photos) {
            String code = p.countryCode();
            if (code != null && !code.isBlank()) {
                codes.add(code);
            }
        }
        if (codes.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2) один bulk-RPC в geo
        List<Country> countries = grpcGeoClient.getByCodes(codes);

        // 3) делаем map code -> Country для быстрого доступа
        Map<String, Country> byCode = new HashMap<>(countries.size());
        for (Country c : countries) {
            byCode.put(c.code(), c);
        }

        // 4) вернуть Map<Photo, Country> в том же порядке входных фото
        Map<Photo, Country> result = new LinkedHashMap<>(photos.size());
        for (Photo p : photos) {
            Country c = byCode.get(p.countryCode());
            if (c != null) {
                result.put(p, c);
            }
        }
        return result;
    }
}
