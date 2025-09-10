package ru.sentidas.rangiffler.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.Country;

import java.util.List;
import java.util.UUID;

@GrpcService
public class GrpcGeoService extends RangifflerGeoServiceGrpc.RangifflerGeoServiceImplBase {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcGeoService.class);

    private final GeoService geoService;

    @Autowired
    public GrpcGeoService(GeoService geoService) {
        this.geoService = geoService;
    }

    @Transactional(readOnly = true)
    @Override
    public void allCountries(com.google.protobuf.Empty request, StreamObserver<CountriesResponse> responseObserver) {
        List<Country> countries = geoService.allCountries();
        responseObserver.onNext(toProto(countries));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void getByCode(CodeRequest request, StreamObserver<CountryResponse> responseObserver) {
        Country country = geoService.getByCode(request.getCode());
        responseObserver.onNext(toProto(country));
        responseObserver.onCompleted();
    }

    @Override
    public void statistics(StatRequest request, io.grpc.stub.StreamObserver<StatResponse> responseObserver) {
        UUID userId = UUID.fromString(request.getUserId());
        boolean withFriends = request.getWithFriends();

        var stats = geoService.statByUserId(userId, withFriends);

        // маппинг в proto
        var respB = StatResponse.newBuilder();
        for (var s : stats) {
            var country = s.country();
            var countryB = CountryResponse.newBuilder()
                    .setCode(country.code())
                    .setName(country.name())
                    .setFlag(country.flag() == null ? "" : country.flag()) // не отдаём null в proto
                    .build();

            respB.addStat(ru.sentidas.rangiffler.grpc.Stat.newBuilder()
                    .setCount(s.count())
                    .setCountry(countryB)
                    .build());
        }

        responseObserver.onNext(respB.build());
        responseObserver.onCompleted();
    }


    public static CountriesResponse toProto(List<Country> countries) {
        CountriesResponse.Builder b = CountriesResponse.newBuilder();
                for(Country country : countries) {
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
