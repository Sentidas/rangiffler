package ru.sentidas.rangiffler.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.qameta.allure.grpc.AllureGrpc;
import ru.sentidas.rangiffler.config.Config;

public abstract class GrpcChannels {

    private static final Config CFG = Config.getInstance();

    private static final Channel photoChannel = ManagedChannelBuilder
            .forAddress(CFG.photoGrpcAddress(), CFG.photoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .intercept(new AllureGrpc())
            .usePlaintext()
            .build();

    private static final Channel geoChannel = ManagedChannelBuilder
            .forAddress(CFG.geoGrpcAddress(), CFG.geoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .intercept(new AllureGrpc())
            .usePlaintext()
            .build();

    private static final Channel userdataChannel = ManagedChannelBuilder
            .forAddress(CFG.userdataGrpcAddress(), CFG.userdataGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .intercept(new AllureGrpc())
            .usePlaintext()
            .build();

    public static final RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub photoBlockingStub
            = RangifflerPhotoServiceGrpc.newBlockingStub(photoChannel);

    public static final RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub geoBlockingStub
            = RangifflerGeoServiceGrpc.newBlockingStub(geoChannel);

    public static final RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub userdataBlockingStub
            = RangifflerUserdataServiceGrpc.newBlockingStub(userdataChannel);

}