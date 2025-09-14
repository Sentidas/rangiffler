package ru.sentidas.rangiffler.service;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.data.repository.impl.CountryRepository;
import ru.sentidas.rangiffler.data.tpl.XaTransactionTemplate;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

import static java.util.Objects.requireNonNull;

@ParametersAreNonnullByDefault
public class GeoDbClient {

    private static final Config CFG = Config.getInstance();

    private final XaTransactionTemplate xaTransactionTemplate = new XaTransactionTemplate(
            CFG.geoJdbcUrl()
    );

    private final CountryRepository countryRepository = new CountryRepository();

    @Step("Get all countries using SQL INSERT")
    @Nonnull
    public List<String> getCountriesCode() {
        return requireNonNull(
                xaTransactionTemplate.execute(
                        countryRepository::findAllCodes)
        );
    }
}

