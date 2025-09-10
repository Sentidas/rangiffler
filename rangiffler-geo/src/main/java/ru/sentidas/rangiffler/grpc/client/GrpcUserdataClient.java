package ru.sentidas.rangiffler.grpc.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.data.domain.Pageable;
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

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUser(request), SERVICE);
        return UserProtoMapper.fromProto(userResponse);
    }



    public String usernameById(UUID userId) {
        var req = UserIdRequest.newBuilder().setUserId(userId.toString()).build();
        var resp = GrpcCall.run(() -> stub.currentUserById(req), SERVICE); // вернёт UserResponse
        return resp.getUsername();
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
