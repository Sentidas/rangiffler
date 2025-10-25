package ru.sentidas.rangiffler.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import ru.sentidas.rangiffler.config.Config;

public abstract class GrpcChannels {

    private static final Config CFG = Config.getInstance();
    private static Long DEADLINE_MS = 0L;

    private static final Channel photoChannel = ManagedChannelBuilder
            .forAddress(CFG.photoGrpcAddress(), CFG.photoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    private static final Channel geoChannel = ManagedChannelBuilder
            .forAddress(CFG.geoGrpcAddress(), CFG.geoGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    private static final Channel userdataChannel = ManagedChannelBuilder
            .forAddress(CFG.userdataGrpcAddress(), CFG.userdataGrpcPort())
            .intercept(new GrpcConsoleInterceptor())
            .usePlaintext()
            .build();

    public static final RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub photoBlockingStub
            = RangifflerPhotoServiceGrpc.newBlockingStub(photoChannel);

    public static final RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub geoBlockingStub
            = RangifflerGeoServiceGrpc.newBlockingStub(geoChannel);

    public static final RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub userdataBlockingStub
            = RangifflerUserdataServiceGrpc.newBlockingStub(userdataChannel);


//    public static RangifflerPhotoServiceGrpc.RangifflerPhotoServiceBlockingStub photoStub() {
//        var stub = RangifflerPhotoServiceGrpc.newBlockingStub(photoChannel);
//
//        return (DEADLINE_MS != null && DEADLINE_MS > 0)
//                ? stub.withDeadline(Deadline.after(DEADLINE_MS, TimeUnit.MILLISECONDS))
//                : stub;
//    }
//
//    public static RangifflerGeoServiceGrpc.RangifflerGeoServiceBlockingStub geoStub() {
//        var stub = RangifflerGeoServiceGrpc.newBlockingStub(geoChannel);
//        return (DEADLINE_MS != null && DEADLINE_MS > 0)
//                ? stub.withDeadline(Deadline.after(DEADLINE_MS, TimeUnit.MILLISECONDS))
//                : stub;
//    }
//
//    public static RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub userdataStub() {
//        var stub = RangifflerUserdataServiceGrpc.newBlockingStub(userdataChannel);
//        return (DEADLINE_MS != null && DEADLINE_MS > 0)
//                ? stub.withDeadline(Deadline.after(DEADLINE_MS, TimeUnit.MILLISECONDS))
//                : stub;
//    }
}