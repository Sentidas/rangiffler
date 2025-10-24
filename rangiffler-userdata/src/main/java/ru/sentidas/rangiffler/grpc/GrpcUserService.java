package ru.sentidas.rangiffler.grpc;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.model.User;
import ru.sentidas.rangiffler.model.UserBulk;
import ru.sentidas.rangiffler.service.FriendshipService;
import ru.sentidas.rangiffler.service.UserService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@GrpcService
public class GrpcUserService extends RangifflerUserdataServiceGrpc.RangifflerUserdataServiceImplBase {

    private final UserService userService;
    private final FriendshipService friendshipService;

    @Value("${app.media.storage.default:BLOB}")
    private String storageMode;

    @Autowired
    public GrpcUserService(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    @Transactional(readOnly = true)
    @Override
    public void currentUser(UsernameRequest request, StreamObserver<UserResponse> responseObserver) {
        User currentUser = userService.getUser(request.getUsername());
        responseObserver.onNext(toProto(currentUser));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void currentUserById(UserIdRequest request, StreamObserver<UserResponse> responseObserver) {
        User currentUser = userService.getUserById(parseUuidOrThrow(request.getUserId()));
        responseObserver.onNext(toProto(currentUser));
        responseObserver.onCompleted();
    }


    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("username is required");
        }
        if (request.hasCountryCode()) {
            normalizeCountryOrThrow(request.getCountryCode());
        }

        User dto = User.fromProto(request);
        User saved = userService.update(dto);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void friendsId(UserIdRequest request, StreamObserver<UserIdsResponse> responseObserver) {
        final UUID userId = parseUuidOrThrow(request.getUserId());

        User currentUser = userService.getUserById(userId);
        List<UUID> ids = userService.friendsId(currentUser.username());
        List<String> asStrings = ids.stream().map(UUID::toString).toList();

        UserIdsResponse response = UserIdsResponse
                .newBuilder()
                .addAllUserIds(asStrings)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void allUsersPage(UserPageRequest request, StreamObserver<UsersPageResponse> responseObserver) {
        final String search = request.hasSearchQuery()
                ? request.getSearchQuery()
                : null;
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Page<UserBulk> users = userService.allUsers(
                request.getUsername(),
                pageable,
                search
        );

        responseObserver.onNext(toProto(users));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void allFriendsPage(UserPageRequest request, StreamObserver<UsersPageResponse> responseObserver) {
        final String search = request.hasSearchQuery()
                ? request.getSearchQuery()
                : null;
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<UserBulk> users = userService.friends(
                request.getUsername(),
                pageable,
                search
        );

        responseObserver.onNext(toProto(users));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void incomeInvitations(UserPageRequest request, StreamObserver<UsersPageResponse> responseObserver) {
        final String search = request.hasSearchQuery()
                ? request.getSearchQuery()
                : null;
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<UserBulk> users = userService.incomeInvitations(
                request.getUsername(),
                pageable,
                search
        );

        responseObserver.onNext(toProto(users));
        responseObserver.onCompleted();
    }

    @Transactional(readOnly = true)
    @Override
    public void outcomeInvitations(UserPageRequest request, StreamObserver<UsersPageResponse> responseObserver) {
        final String search = request.hasSearchQuery()
                ? request.getSearchQuery()
                : null;
        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize()
        );

        Slice<UserBulk> users = userService.outcomeInvitations(
                request.getUsername(),
                pageable,
                search
        );

        responseObserver.onNext(toProto(users));
        responseObserver.onCompleted();
    }

    @Override
    public void removeFriend(FriendshipRequest request, StreamObserver<Empty> responseObserver) {

        friendshipService.deleteFriend(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void createFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User sendInvitationRequest = friendshipService.sendInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(sendInvitationRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void acceptFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User acceptInvitationRequest = friendshipService.acceptInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(acceptInvitationRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void declineFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User declineInvitationRequest = friendshipService.declineInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(declineInvitationRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void usernamesByIds(UserIdsRequest request, io.grpc.stub.StreamObserver<UsernamesResponse> out) {
        List<UUID> ids = new java.util.ArrayList<>(request.getUserIdsCount());
        for (String s : request.getUserIdsList()) {
            try {
                ids.add(java.util.UUID.fromString(s));
            } catch (Exception ignore) {
            }
        }

        Map<UUID, String> map = userService.usernamesByIds(ids);

        UsernamesResponse.Builder rb = UsernamesResponse.newBuilder();
        for (Map.Entry<UUID, String> e : map.entrySet()) {
            rb.addItems(
                    UsernamesResponse.Item.newBuilder()
                            .setUserId(e.getKey().toString())
                            .setUsername(e.getValue() == null ? "" : e.getValue())
                            .build()
            );
        }
        out.onNext(rb.build());
        out.onCompleted();
    }

    @Override
    public void getStorageMode(Empty request, StreamObserver<UserStorageModeResponse> responseObserver) {
        responseObserver.onNext(UserStorageModeResponse.newBuilder()
                .setMode(storageMode.toUpperCase())
                .build());
        responseObserver.onCompleted();
    }


    private static UserResponse toProto(User user) {
        UserResponse.Builder b = UserResponse.newBuilder();
        user.toProto(b);
        return b.build();
    }

    private static UserResponse toProto(UserBulk u) {
        UserResponse.Builder b = UserResponse.newBuilder();
        u.toProto(b);
        return b.build();
    }

    private static int calcTotalPagesForSlice(Slice<?> s) {
        return s.getNumber() + 1 + (s.hasNext() ? 1 : 0);
    }

    private static UsersPageResponse toProto(Slice<UserBulk> page) {
        UsersPageResponse.Builder b = UsersPageResponse.newBuilder();
        b.setTotalElements(page.getNumberOfElements());
        b.setTotalPages(calcTotalPagesForSlice(page));
        b.setFirst(page.isFirst());
        b.setLast(page.isLast());
        b.setPage(page.getNumber());
        b.setSize(page.getSize());
        for (UserBulk u : page.getContent()) {
            b.addContent(toProto(u));
        }
        return b.build();
    }

    private static UUID parseUuidOrThrow(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("user_id is required");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("user_id must be a valid UUID");
        }
    }

    private static String normalizeCountryOrThrow(String code) {
        String normalized = code.trim().toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z]{2}$")) {
            throw new IllegalArgumentException("country_code must be two lowercase letters (ISO 3166-1 alpha-2), e.g. 'fr'");
        }
        return normalized;
    }
}
