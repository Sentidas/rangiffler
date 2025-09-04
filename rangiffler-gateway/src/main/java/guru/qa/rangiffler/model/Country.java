package guru.qa.rangiffler.model;

import guru.qa.rangiffler.entity.CountryEntity;

public record Country(

        String code,
        String name,
        String flag
) {
    public static Country fromEntity(CountryEntity entity) {
        return new Country(
                entity.getCode(),
                entity.getName(),
                entity.getFlag());
    }
}