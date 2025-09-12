package ru.sentidas.rangiffler.grpc;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.transaction.annotation.Transactional;
import ru.sentidas.rangiffler.model.User;
import ru.sentidas.rangiffler.model.UserBulk;
import ru.sentidas.rangiffler.service.UserService;


import java.util.List;
import java.util.UUID;


@GrpcService
public class GrpcUserService extends RangifflerUserdataServiceGrpc.RangifflerUserdataServiceImplBase {

    private final UserService userService;

    @Autowired
    public GrpcUserService(UserService userService) {
        this.userService = userService;

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
        User currentUser = userService.getUserById(UUID.fromString(request.getUserId()));
        responseObserver.onNext(toProto(currentUser));
        responseObserver.onCompleted();
    }


    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        User dto = User.fromProto(request);
        User saved = userService.update(dto);
        responseObserver.onNext(toProto(saved));
        responseObserver.onCompleted();
    }

    @Override
    public void friendsId(UserIdRequest request, StreamObserver<UserIdsResponse> responseObserver) {
        String userIdStr = request.getUserId();
        if (userIdStr == null || userIdStr.isBlank()) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("user_id is required").asRuntimeException());
            return;
        }

        final UUID userId;
        try {
            userId = UUID.fromString(userIdStr.trim());
        } catch (IllegalArgumentException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("user_id must be a valid UUID").asRuntimeException());
            return;
        }

        // можно сразу сделать метод в сервисе: friendsIdByUserId(userId)
        // но раз уже есть friendsId(username), достанем username:
        User currentUser = userService.getUserById(userId);
        List<UUID> ids = userService.friendsId(currentUser.username());

        List<String> asStrings = ids.stream().map(UUID::toString).toList();

        UserIdsResponse response = UserIdsResponse.newBuilder()
                .addAllUserIds(asStrings)         // ВАЖНО: repeated -> addAll...
                .build();

        responseObserver.onNext(response);    // отправляем то, что собрали
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

        userService.deleteFriend(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void createFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User sendInvitationRequest = userService.sendInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(sendInvitationRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void acceptFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User acceptInvitationRequest = userService.acceptInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(acceptInvitationRequest));
        responseObserver.onCompleted();
    }

    @Override
    public void declineFriendshipRequest(FriendshipRequest request, StreamObserver<UserResponse> responseObserver) {

        User declineInvitationRequest = userService.declineInvitation(
                request.getUsername(),
                request.getUser()
        );

        responseObserver.onNext(toProto(declineInvitationRequest));
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
        // номер страницы 0-based; если есть следующая, добавляем ещё одну "виртуальную" страницу
        return s.getNumber() + 1 + (s.hasNext() ? 1 : 0);
    }
    private static UsersPageResponse toProto(Slice<UserBulk> page) {
        UsersPageResponse.Builder b = UsersPageResponse.newBuilder();
        b.setTotalElements(page.getNumberOfElements());
        b.setTotalPages(calcTotalPagesForSlice(page)); // вместо page.getPageable().getPageSize()
        b.setFirst(page.isFirst());
        b.setLast(page.isLast());
        b.setPage(page.getNumber());
        b.setSize(page.getSize());
        for (UserBulk u : page.getContent()) {
            b.addContent(toProto(u));
        }
        return b.build();
    }
}
