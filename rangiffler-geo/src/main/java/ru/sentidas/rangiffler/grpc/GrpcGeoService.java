package ru.sentidas.rangiffler.grpc;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.service.GeoService;
import ru.sentidas.rangiffler.utils.CountryCodeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ru.sentidas.rangiffler.grpc.Stat.newBuilder;

@GrpcService
public class GrpcGeoService extends RangifflerGeoServiceGrpc.RangifflerGeoServiceImplBase {

    private final GeoService geoService;

    @Autowired
    public GrpcGeoService(GeoService geoService) {
        this.geoService = geoService;
    }

    @Transactional(readOnly = true)
    @Override
    public void allCountries(com.google.protobuf.Empty request, StreamObserver<CountriesResponse> responseObserver) {
        final List<Country> countries = geoService.allCountries();
        responseObserver.onNext(toProto(countries));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void getByCode(CodeRequest request, StreamObserver<CountryResponse> responseObserver) {
        final String normalizedCountryCode = CountryCodeValidator.normalizeAndValidate(request.getCode());

        final Country country = geoService.getByCode(normalizedCountryCode);
        responseObserver.onNext(toProto(country));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void getByCodes(CodesRequest request, StreamObserver<CountriesResponse> responseObserver) {
        final List<String> requestedCountryCodes = request.getCodesList();
        final List<String> normalizedCountryCodes = new ArrayList<>();

        for (String requestedCode : requestedCountryCodes) {
            String normalizedCountryCode = CountryCodeValidator.normalizeAndValidate(requestedCode);
            normalizedCountryCodes.add(normalizedCountryCode);
        }

        final List<Country> countries = geoService.getByCodes(normalizedCountryCodes);
        responseObserver.onNext(toProto(countries));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void statistics(StatRequest request, StreamObserver<StatResponse> responseObserver) {
        final UUID userId = UUID.fromString(request.getUserId());
        final boolean withFriends = request.getWithFriends();

        final List<Stat> stats = geoService.statByUserId(userId, withFriends);

        StatResponse.Builder responseBuilder = StatResponse.newBuilder();
        for (Stat s : stats) {
            final Country country = s.country();
            CountryResponse.Builder countryResponse = CountryResponse.newBuilder()
                    .setCode(country.code())
                    .setName(country.name())
                    .setFlag(country.flag());

            responseBuilder.addStat(newBuilder()
                    .setCount(s.count())
                    .setCountry(countryResponse.build())
                    .build());
        }
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    public static CountriesResponse toProto(List<Country> countries) {
        CountriesResponse.Builder b = CountriesResponse.newBuilder();
        for (Country country : countries) {
            b.addCountries(toProto(country));
        }
        return b.build();
    }

    private static CountryResponse toProto(Country country) {
        CountryResponse.Builder b = CountryResponse.newBuilder();
        country.toProto(b);
        return b.build();
    }
}
