package ru.sentidas.rangiffler.test.grpc.userdata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.FriendStatus;
import ru.sentidas.rangiffler.grpc.UserPageRequest;
import ru.sentidas.rangiffler.grpc.UserResponse;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.*;

@GrpcTest
@DisplayName("Grpc_Userdata: allUsersPage")
public class AllUsersTest extends BaseTest {

    @Test
    @DisplayName("Список пользователей: отражает статусы отношений при наличии друзей и инвайтов")
    @User(friends = 2, incomeInvitation = 1, outcomeInvitation = 1)
    void allUsersPageReflectsFriendStatusesWhenThereAreFriendsAndInvites(AppUser user) {
        final List<String> myFriendIds = friendIdsToString(user);
        final String inviterId = incomingInviterIdToString(user);
        final String inviteeId = outgoingInviteeIdToString(user);

        UsersPageResponse response = userdataBlockingStub.allUsersPage(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(50)
                        .build()
        );

        // В выдаче не должен присутствовать сам пользователь
        assertFalse(response.getContentList().stream().anyMatch(u -> u.getId().equals(user.id().toString())),
                "self user must be excluded from allUsers");

        // Друзья -> FRIEND
        for (UserResponse item : response.getContentList()) {
            if (myFriendIds.contains(item.getId())) {
                assertTrue(item.hasFriendStatus(), "friend_status should be present for friends");
                assertEquals(FriendStatus.FRIEND, item.getFriendStatus(), "friend_status should be FRIEND");
            }
        }

        // Входящий -> INVITATION_RECEIVED
        assertTrue(response.getContentList().stream()
                        .filter(u -> u.getId().equals(inviterId))
                        .allMatch(u -> u.hasFriendStatus() && u.getFriendStatus() == FriendStatus.INVITATION_RECEIVED),
                "inviter should have INVITATION_RECEIVED");

        // Исходящий -> INVITATION_SENT
        assertTrue(response.getContentList().stream()
                        .filter(u -> u.getId().equals(inviteeId))
                        .allMatch(u -> u.hasFriendStatus() && u.getFriendStatus() == FriendStatus.INVITATION_SENT),
                "invitee should have INVITATION_SENT");
    }

    @Test
    @DisplayName("Список пользователей: фильтруется по поиску, когда задан searchQuery")
    @User(friends = 2, incomeInvitation = 1, outcomeInvitation = 1)
    void allUsersPageIsFilteredBySearchQueryWhenSearchQueryProvided(AppUser user) {
        final String friendUsername = firstFriendUsername(user);

        UsersPageResponse filtered = userdataBlockingStub.allUsersPage(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(50)
                        .setSearchQuery(friendUsername)
                        .build()
        );

        assertAll("Filtered content",
                () -> assertTrue(filtered.getContentCount() >= 1, "filtered list should not be empty"),
                () -> assertTrue(filtered.getContentList().stream().allMatch(u -> u.getUsername().contains(friendUsername)),
                        "all items should match search query")
        );
    }

    @Test
    @DisplayName("Список пользователей: корректная пагинация, когда пользователей больше размера страницы")
    @User(friends = 2, incomeInvitation = 1, outcomeInvitation = 1)
    void allUsersPagePaginatedCorrectlyWhenMoreThanPageSize(AppUser user) {
        UsersPageResponse page0 = userdataBlockingStub.allUsersPage(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(1)
                        .build()
        );
        final boolean hasMoreThanPage0 = page0.getTotalElements() > 1 || !page0.getLast();

        assertAll("Page 0",
                () -> assertEquals(1, page0.getSize(), "size should be 1 on page 0"),
                () -> assertEquals(0, page0.getPage(), "page index should be 0"),
                () -> assertTrue(page0.getFirst(), "first should be true on first page")
        );

        if (hasMoreThanPage0) {
            UsersPageResponse page1 = userdataBlockingStub.allUsersPage(
                    UserPageRequest.newBuilder()
                            .setUsername(user.username())
                            .setPage(1)
                            .setSize(1)
                            .build()
            );

            assertAll("Page 1",
                    () -> assertEquals(1, page1.getSize(), "size should be 1 on page 1"),
                    () -> assertEquals(1, page1.getPage(), "page index should be 1"),
                    () -> assertFalse(page1.getFirst(), "first should be false on page 1")
            );
        }
    }
}
