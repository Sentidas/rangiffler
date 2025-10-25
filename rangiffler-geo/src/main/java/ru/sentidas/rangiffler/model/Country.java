package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.entity.CountryEntity;
import ru.sentidas.rangiffler.grpc.CountryResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public record Country(
        UUID id,
        String code,
        String name,
        String flag
) {

    public static Country fromEntity(CountryEntity country) {
        String flagDataUrl = country.getFlag() == null
                ? null
                : new String(country.getFlag(), StandardCharsets.UTF_8);

        return new Country(
                country.getId(),
                country.getCode(),
                country.getName(),
                flagDataUrl
        );
    }

    public static List<Country> fromEntity(List<CountryEntity> countries) {
        return countries.stream()
                .map(Country::fromEntity)
                .toList();
    }

    public void toProto(CountryResponse.Builder b) {
        if (id != null) b.setId(id.toString());
        if (code != null) b.setCode(code);
        if (name != null) b.setName(name);
        if (flag != null) b.setFlag(flag);// base64 строка
    }
}