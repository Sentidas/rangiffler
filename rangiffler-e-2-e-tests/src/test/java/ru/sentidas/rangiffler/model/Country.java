package ru.sentidas.rangiffler.model;

import ru.sentidas.rangiffler.DataUrl;
import ru.sentidas.rangiffler.data.entity.geo.CountryEntity;

import java.util.UUID;

public record Country(
        UUID id,
        String code,
        String name,
        String flag
) {

    public CountryEntity toEntity() {
        CountryEntity e = new CountryEntity();
        e.setId(id);
        e.setCode(code);
        e.setName(name);
        e.setFlag(flagToBytes(flag)); // bytes only в БД
        return e;
    }

    public static Country fromEntity(CountryEntity e) {
        return new Country(
                e.getId(),
                e.getCode(),
                e.getName(),
                bytesToDataUrl(e.getFlag()) // data:URL обратно в модель
        );
    }

    private static byte[] flagToBytes(String dataUrl) {
        DataUrl parsed = DataUrl.parse(dataUrl);
        return parsed.bytes();
    }

    private static String bytesToDataUrl(byte[] bytes) {
        String defaultMime = "image/png";
        return DataUrl.build(defaultMime, bytes);
    }
}