package ru.sentidas.rangiffler.service;

import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.Stat;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public interface GeoClient {

    Country getByCode(String code);

    List<Country> allCountries();

    List<Stat> statistics(UUID userId, boolean withFriends);

    @Nonnull
    default List<String> getCountriesCode() {
        return allCountries().stream().map(Country::code).collect(Collectors.toList());
    }
}
