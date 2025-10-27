package ru.sentidas.rangiffler.service.api;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Component;
import ru.sentidas.rangiffler.grpc.*;
import ru.sentidas.rangiffler.model.UserGql;
import ru.sentidas.rangiffler.model.input.FriendshipInput;
import ru.sentidas.rangiffler.model.input.UserGqlInput;
import ru.sentidas.rangiffler.service.utils.GrpcCall;
import ru.sentidas.rangiffler.service.utils.ProtoMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GrpcUserdataClient {

    private static final String SERVICE = "rangiffler-userdata";

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @GrpcClient("grpcUserdataClient")
    private RangifflerUserdataServiceGrpc.RangifflerUserdataServiceBlockingStub stub;

    /**
     * ОДИН bulk-вызов: список UUID → Map<UUID, username>
     */
    public Map<UUID, String> getUsernamesByIds(List<UUID> ids) {

        UserIdsRequest.Builder rb = UserIdsRequest.newBuilder();
        for (UUID id : ids) {
            if (id != null) {
                rb.addUserIds(id.toString());
            }
        }

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

    public UserGql currentUser(String username) {

        UsernameRequest request = UsernameRequest.newBuilder()
                .setUsername(username)
                .build();

        UserResponse userResponse = GrpcCall.run(() -> stub.currentUser(request), SERVICE);
        return ProtoMapper.fromProto(userResponse, publicBaseUrl);
    }

    public Slice<UserGql> friends(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.allFriendsPage(request), SERVICE);
        return ProtoMapper.fromProto(response, publicBaseUrl);
    }

    public Slice<UserGql> incomeInvitations(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.incomeInvitations(request), SERVICE);
        return ProtoMapper.fromProto(response, publicBaseUrl);
    }

    public Slice<UserGql> outcomeInvitations(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.outcomeInvitations(request), SERVICE);
        return ProtoMapper.fromProto(response, publicBaseUrl);
    }

    public Slice<UserGql> allUsers(String username, PageRequest pageRequest, String searchQuery) {
        UserPageRequest request = buildPageRequest(username, pageRequest, searchQuery);
        UsersPageResponse response = GrpcCall.run(() -> stub.allUsersPage(request), SERVICE);
        return ProtoMapper.fromProto(response, publicBaseUrl);
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
        return ProtoMapper.fromProto(response, publicBaseUrl);
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
        return ProtoMapper.fromProto(response, publicBaseUrl);
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
        return ProtoMapper.fromProto(response, publicBaseUrl);
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
        return ProtoMapper.fromProto(response, publicBaseUrl);
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
