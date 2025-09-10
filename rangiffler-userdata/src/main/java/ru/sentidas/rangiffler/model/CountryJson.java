//package ru.sentidas.rangiffler.model;
//
//
//import ru.sentidas.rangiffler.data.entity.CountryEntity;
//import ru.sentidas.rangiffler.utils.BytesAsString;
//
//import java.util.UUID;
//
//public record CountryJson(
//        UUID id,
//        String code,
//        String name,
//        String flag
//) {
//    public static CountryJson fromEntity(CountryEntity country) {
//        return new CountryJson(
//                country.getId(),
//                country.getCode(),
//                country.getName(),
//                new BytesAsString(country.getFlag()).string());
//    }
//}