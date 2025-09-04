package guru.qa.rangiffler.service.api;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.RangifflerUserdataServiceGrpc;

@Component
public class GrpcUserdataClient {

    @GrpcClient("grpcUserdataClient")
    private RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub rangifflerUserdataServiceBlockingStub;




}
