package ru.sentidas.rangiffler.test.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.grpc.GrpcConsoleInterceptor;
import ru.sentidas.rangiffler.grpc.RangifflerGeoServiceGrpc;
import ru.sentidas.rangiffler.grpc.RangifflerPhotoServiceGrpc;
import ru.sentidas.rangiffler.grpc.RangifflerUserdataServiceGrpc;

public abstract class BaseTest {

    protected static final Config CFG = Config.getInstance();

    protected static final Channel photoChannel = ManagedChannelBuilder
            .forAddress(CFG.photoGrpcAddress(), CFG.photoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    protected static final Channel geoChannel = ManagedChannelBuilder
            .forAddress(CFG.geoGrpcAddress(), CFG.geoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    public static final Channel userdataChannel = ManagedChannelBuilder
            .forAddress(CFG.userdataGrpcAddress(), CFG.userdataGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    protected static final RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub photoBlockingStub
            = RangifflerPhotoServiceGrpc.newBlockingStub(photoChannel);

    protected static final RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub geoBlockingStub
            = RangifflerGeoServiceGrpc.newBlockingStub(geoChannel);

    protected static final RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub userdataBlockingStub
            = RangifflerUserdataServiceGrpc.newBlockingStub(userdataChannel);
}
