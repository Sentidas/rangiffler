package ru.sentidas.rangiffler.grpc.client;


import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.grpc.utils.GrpcCall;

import java.util.List;
import java.util.UUID;

@Component
public class GrpcUserdataClient {

    private static final String SERVICE = "rangiffler-userdata";

    @GrpcClient("grpcUserdataClient")
    private RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub stub;

    public String getUsernameById(UUID userId) {

        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUserById(request), SERVICE);
        return userResponse.getUsername();
    }

    public UUID getIdByUsername(String username) {

        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUser(request), SERVICE);
        return UUID.fromString(userResponse.getId());
    }


    public List<UUID> friendIdsAll(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserIdsResponse response = GrpcCall.run(() -> stub.friendsId(request), SERVICE);
        return response.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();
    }


}
