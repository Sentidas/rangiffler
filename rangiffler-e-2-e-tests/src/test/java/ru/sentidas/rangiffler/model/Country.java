package ru.sentidas.rangiffler.model;
import ru.sentidas.rangiffler.data.entity.geo.CountryEntity;
import ru.sentidas.rangiffler.grpc.CountryResponse;
import ru.sentidas.rangiffler.utils.BytesAsString;


import java.util.List;
import java.util.UUID;

public record Country(
        UUID id,
        String code,
        String name,
        String flag
) {

    public static Country fromEntity(CountryEntity country) {


        return new Country(
                country.getId(),
                country.getCode(),
                country.getName(),
                new BytesAsString(country.getFlag()).string());
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