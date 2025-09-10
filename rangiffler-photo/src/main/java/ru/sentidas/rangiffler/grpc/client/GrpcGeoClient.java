//package ru.sentidas.rangiffler.grpc.client;
//
//import com.google.protobuf.Empty;
//import net.devh.boot.grpc.client.inject.GrpcClient;
//import org.springframework.stereotype.Component;
//import ru.sentidas.rangiffler.grpc.CodeRequest;
//import ru.sentidas.rangiffler.grpc.CountriesResponse;
//import ru.sentidas.rangiffler.grpc.CountryResponse;
//import ru.sentidas.rangiffler.grpc.RangifflerGeoServiceGrpc;
//import ru.sentidas.rangiffler.grpc.utils.GrpcCall;
//
//import java.util.List;
//
//
//@Component
//public class GrpcGeoClient {
//
//    private static final String SERVICE = "rangiffler-geo";
//    private static final Empty EMPTY = Empty.getDefaultInstance();
//
//    @GrpcClient("grpcGeoClient")
//    private RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub stub;
//
//
//    @Override
//    // @Cacheable(cacheNames =  "countries", key = "#code")  // надо включить кэш если использовать
//    public String getByCode(String code) {
//        CodeRequest request = CodeRequest.newBuilder()
//                .setCode(code)
//                .build();
//
//        CountryResponse response = GrpcCall.run(() -> stub.getByCode(request), SERVICE);
//        return fromProto(response);
//    }
//
//    @Override
//    public List<CountryGql> countries() {
//        CountriesResponse countries = GrpcCall.run(() -> stub.allCountries(EMPTY), SERVICE);
//        return fromProto(countries);
//    }
//
//    public static CountryGql fromProto(CountryResponse response) {
//        return new CountryGql(
//                response.getCode(),
//                response.getName(),
//                response.getFlag()
//        );
//    }
//    public static List<CountryGql> fromProto(CountriesResponse response) {
//        return response.getCountriesList().stream()
//                .map(GrpcGeoClient::fromProto)
//                .toList();
//    }
//}
