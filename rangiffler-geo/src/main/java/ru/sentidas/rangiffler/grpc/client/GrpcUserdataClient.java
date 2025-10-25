package ru.sentidas.rangiffler.grpc.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.User;

import java.util.List;
import java.util.UUID;

@Component
public class GrpcUserdataClient {

    private static final String SERVICE = "rangiffler-userdata";

    @GrpcClient("grpcUserdataClient")
    private RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub stub;

    public User currentUser(String username) {
        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();
        UserResponse userResponse = stub.currentUser(request);
        return UserProtoMapper.fromProto(userResponse);
    }

    public String usernameById(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder().setUserId(userId.toString()).build();
        UserResponse response = stub.currentUserById(request);
        return response.getUsername();
    }

    public List<UUID> friendIdsAll(UUID userId) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(userId.toString())
                .build();

        UserIdsResponse response = stub.friendsId(request);
        return response.getUserIdsList().stream()
                .map(UUID::fromString)
                .toList();
    }
}
