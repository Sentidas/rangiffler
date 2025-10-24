package ru.sentidas.rangiffler.service.impl;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.repository.impl.CountryRepositoryImpl;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.service.GeoClient;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class GeoDbClient implements GeoClient {

    private static final Config CFG = Config.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.geoJdbcUrl()
    );

    private final CountryRepositoryImpl countryRepository = new CountryRepositoryImpl();


    @Override
    @Step("Get country by code '{0}' using SQL")
    @Nonnull
    public Country getByCode(String code) {
        return requireNonNull(
                xaTransactionTemplate.execute(() ->
                        countryRepository.findByCode(code)
                                .map(Country::fromEntity)
                                .orElseThrow(() ->
                                        new IllegalArgumentException("Country not found by code=" + code)
                                )
                )
        );
    }

    @Override
    @Step("Get all countries using SQL (via codes)")
    @Nonnull
    public List<Country> allCountries() {
        return requireNonNull(
                xaTransactionTemplate.execute(() -> {
                    List<String> codes = countryRepository.findAllCodes();
                    return codes.stream()
                            .map(c -> countryRepository.findByCode(c)
                                    .map(Country::fromEntity)
                                    .orElseThrow(() ->
                                            new IllegalStateException("Country listed in codes but not found: " + c)
                                    )
                            )
                            .collect(Collectors.toList());
                })
        );
    }

    @Override
    @Step("Get statistics using SQL: userId={0}, withFriends={1}")
    @Nonnull
    public List<Stat> statistics(UUID userId, boolean withFriends) {
        throw new UnsupportedOperationException("DB statistics is not implemented");
    }
}

