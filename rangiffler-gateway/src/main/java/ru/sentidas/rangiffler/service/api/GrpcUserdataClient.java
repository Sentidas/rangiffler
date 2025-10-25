package ru.sentidas.rangiffler.service.api;

import org.springframework.beans.factory.annotation.Autowired;
import ru.sentidas.rangiffler.model.input.FriendshipInput;
import ru.sentidas.rangiffler.model.UserGql;
import ru.sentidas.rangiffler.model.input.UserGqlInput;
import ru.sentidas.rangiffler.service.utils.GrpcCall;
import ru.sentidas.rangiffler.service.utils.UserProtoMapper;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GrpcUserdataClient {

    private static final String SERVICE = "rangiffler-userdata";

    @Autowired
    public GrpcUserdataClient(
            @Value("${app.public-base-url:}") String baseUrl
    ) {
        UserProtoMapper.setBaseUrl(baseUrl);
    }

    /**
     * ОДИН bulk-вызов: список UUID → Map<UUID, username>
     */
    public Map<UUID, String> getUsernamesByIds(List<UUID> ids) {
        // строим proto-запрос
        UserIdsRequest.Builder rb = UserIdsRequest.newBuilder();
        for (UUID id : ids) {
            if (id != null) {
                rb.addUserIds(id.toString());
            }
        }
        // один gRPC-вызов
        UsernamesResponse resp = GrpcCall.run(() -> stub.usernamesByIds(rb.build()), SERVICE);

        Map<UUID, String> result = new HashMap<>(resp.getItemsCount());
        for (UsernamesResponse.Item it : resp.getItemsList()) {
            result.put(UUID.fromString(it.getUserId()), it.getUsername());
        }
        return result;
    }

    public String getUsernameById(UUID id) {
        UserIdRequest request = UserIdRequest.newBuilder()
                .setUserId(id.toString())
                .build();

        UserResponse resp = GrpcCall.run(() -> stub.currentUserById(request), SERVICE);
        return resp.getUsername();
    }

    @GrpcClient("grpcUserdataClient")
    private RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub stub;

    public UserGql currentUser(String username) {

        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUser(request), SERVICE);
        return UserProtoMapper.fromProto(userResponse);
    }

    public Slice<UserGql> friends(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.allFriendsPage(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public Slice<UserGql> incomeInvitations(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.incomeInvitations(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public Slice<UserGql> outcomeInvitations(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.outcomeInvitations(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public Slice<UserGql> allUsers(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.allUsersPage(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public UserGql updateUser(String username, UserGqlInput input) {
        UpdateUserRequest.Builder request = UpdateUserRequest.newBuilder()
                .setUsername(username);

        if (input.firstname() != null && !input.firstname().isBlank()) request.setFirstname(input.firstname());
        if (input.surname() != null && !input.surname().isBlank()) request.setSurname(input.surname());
        if (input.avatar() != null && !input.avatar().isBlank()) request.setAvatar(input.avatar());

        if (input.location() != null && input.location().code() != null && !input.location().code().isBlank()) {
            request.setCountryCode(input.location().code());
        }

        UserResponse response = GrpcCall.run(() -> stub.updateUser(request.build()), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public UUID getIdByUsername(String username) {

        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUser(request), SERVICE);
        return UUID.fromString(userResponse.getId());
    }

    public UserGql deleteFriend(String username, FriendshipInput friendshipInput) {
        if (friendshipInput.user() == null) {
            throw new IllegalArgumentException("ID targetUsername is required");
        }

        String friendShipInputUsername = getUsernameById(friendshipInput.user());

        FriendshipRequest request = FriendshipRequest.newBuilder()
                .setUsername(username)
                .setUser(friendshipInput.user().toString())
                .build();
        GrpcCall.runVoid(() -> stub.removeFriend(request), SERVICE);

        UserGql userGql = new UserGql(
                friendshipInput.user(),
                friendShipInputUsername, null, null, null, null, null, null);

        return userGql;
    }

    public UserGql sendInvitation(String username, FriendshipInput friendshipInput) {
        if (friendshipInput.user() == null) {
            throw new IllegalArgumentException("ID targetUsername is required");
        }
        FriendshipRequest request = FriendshipRequest.newBuilder()
                .setUsername(username)
                .setUser(friendshipInput.user().toString())
                .build();
        UserResponse response = GrpcCall.run(() -> stub.createFriendshipRequest(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public UserGql acceptInvitation(String username, FriendshipInput friendshipInput) {
        if (friendshipInput.user() == null) {
            throw new IllegalArgumentException("ID targetUsername is required");
        }
        FriendshipRequest request = FriendshipRequest.newBuilder()
                .setUsername(username)
                .setUser(friendshipInput.user().toString())
                .build();
        UserResponse response = GrpcCall.run(() -> stub.acceptFriendshipRequest(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    public UserGql rejectInvitation(String username, FriendshipInput friendshipInput) {
        if (friendshipInput.user() == null) {
            throw new IllegalArgumentException("ID targetUsername is required");
        }
        FriendshipRequest request = FriendshipRequest.newBuilder()
                .setUsername(username)
                .setUser(friendshipInput.user().toString())
                .build();
        UserResponse response = GrpcCall.run(() -> stub.declineFriendshipRequest(request), SERVICE);
        return UserProtoMapper.fromProto(response);
    }

    private static UserPageRequest buildPageRequest(String username, PageRequest page, String searchQuery) {
        UserPageRequest.Builder builder = UserPageRequest.newBuilder()
                .setUsername(username)
                .setPage(page.getPageNumber())
                .setSize(page.getPageSize());
        if (searchQuery != null && !searchQuery.isBlank()) {
            builder.setSearchQuery(searchQuery);
        }
        return builder.build();
    }
}
