package ru.sentidas.rangiffler.service.impl;

import io.qameta.allure.Step;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.service.GeoClient;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ParametersAreNonnullByDefault
public class GeoApiClient implements GeoClient {

    @SuppressWarnings("unused")
    private static final Config CFG = Config.getInstance();

    private final RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub stub;

    public GeoApiClient() {
        this.stub = GrpcChannels.geoBlockingStub;
    }


    @Override
    @Step("Get country by code '{0}' using gRPC")
    @Nonnull
    public Country getByCode(String code) {
        CodeRequest req = CodeRequest.newBuilder().setCode(code).build();
        CountryResponse resp = stub.getByCode(req);
        return toCountry(resp);
    }

    @Override
    @Step("Get all countries using gRPC")
    @Nonnull
    public List<Country> allCountries() {
        CountriesResponse resp = stub.allCountries(com.google.protobuf.Empty.getDefaultInstance());
        return resp.getCountriesList()
                .stream()
                .map(GeoApiClient::toCountry)
                .collect(Collectors.toList());
    }

    @Override
    @Step("Get statistics using gRPC: userId={0}, withFriends={1}")
    @Nonnull
    public List<Stat> statistics(UUID userId, boolean withFriends) {
        StatRequest req = StatRequest.newBuilder()
                .setUserId(userId.toString())
                .setWithFriends(withFriends)
                .build();
        StatResponse resp = stub.statistics(req);
        return resp.getStatList()
                .stream()
                .map(GeoApiClient::toGeoStat)
                .collect(Collectors.toList());
    }

    @Nonnull
    private static Country toCountry(CountryResponse r) {
        return new Country(
                UUID.fromString(r.getId()),
                r.getCode(), r.getName(),
                r.getFlag());
    }

    @Nonnull
    private static Stat toGeoStat(ru.sentidas.rangiffler.grpc.Stat stat) {
        return new Stat(
                stat.getCount(),
                toCountry(stat.getCountry()));
    }
}
