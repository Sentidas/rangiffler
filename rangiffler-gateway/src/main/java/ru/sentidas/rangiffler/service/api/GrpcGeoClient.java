package ru.sentidas.rangiffler.service.api;

import com.google.protobuf.Empty;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.service.utils.GrpcCall;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class GrpcGeoClient {

    private static final String SERVICE = "rangiffler-geo";
    private static final Empty EMPTY = Empty.getDefaultInstance();
    private final GrpcUserdataClient grpcUserdataClient;

    @GrpcClient("grpcGeoClient")
    private RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub stub;

    public GrpcGeoClient(GrpcUserdataClient grpcUserdataClient) {
        this.grpcUserdataClient = grpcUserdataClient;
    }

    public Country getByCode(String code) {
        CodeRequest request = CodeRequest.newBuilder()
                .setCode(code)
                .build();

        CountryResponse response = GrpcCall.run(() -> stub.getByCode(request), SERVICE);
        return fromProto(response);
    }

    public List<Country> getByCodes(List<String> codes) {
        CodesRequest request = CodesRequest.newBuilder()
                .addAllCodes(codes)
                .build();

        CountriesResponse response = GrpcCall.run(() -> stub.getByCodes(request), SERVICE);
        return fromProto(response);
    }

    public List<Country> countries() {
        CountriesResponse countries = GrpcCall.run(() -> stub.allCountries(EMPTY), SERVICE);
        return fromProto(countries);
    }

    public List<Stat> stat(String username, boolean withFriends) {
        String userId = grpcUserdataClient.currentUser(username).id().toString();
        StatRequest request = StatRequest.newBuilder()
                .setUserId(userId)
                .setWithFriends(withFriends)
                .build();
        StatResponse statList = GrpcCall.run(() -> stub.statistics(request), SERVICE);
        return fromProto(statList);
    }

    public static Country fromProto(CountryResponse response) {
        return new Country(
                response.getCode(),
                response.getName(),
                response.getFlag()
        );
    }

    public static List<Stat> fromProto(StatResponse response) {
        return  response.getStatList().stream()
                .map(GrpcGeoClient::fromProto).toList();
    }

    public static Stat fromProto(ru.sentidas.rangiffler.grpc.Stat stat) {
        Country country = new Country(
                stat.getCountry().getCode(),
                stat.getCountry().getName(),
                stat.getCountry().getFlag()
        );
        return new Stat(
                stat.getCount(),
                country
        );
    }
    public static List<Country> fromProto(CountriesResponse response) {
        return response.getCountriesList().stream()
                .map(GrpcGeoClient::fromProto)
                .toList();
    }
}
