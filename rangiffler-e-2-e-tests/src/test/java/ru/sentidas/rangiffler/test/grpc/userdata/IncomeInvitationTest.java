package ru.sentidas.rangiffler.test.grpc.userdata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.UserPageRequest;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Grpc_Userdata: incomeInvitations")
public class IncomeInvitationTest extends BaseTest {

    @Test
    @DisplayName("Входящие приглашения: корректная пагинация при нескольких страницах")
    @User(incomeInvitation = 3)
    void incomeInvitationsAreListedAndPaginatedWhenMultipleInvites(AppUser user) {
        UsersPageResponse page0 = userdataBlockingStub.incomeInvitations(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(2)
                        .build()
        );
        assertAll("Income page 0",
                () -> assertEquals(2, page0.getContentCount(), "content size should be 2"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertFalse(page0.getLast(), "last should be false")
        );

        UsersPageResponse page1 = userdataBlockingStub.incomeInvitations(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(1)
                        .setSize(2)
                        .build()
        );
        assertAll("Income page 1",
                () -> assertTrue(page1.getContentCount() >= 1, "content should have remaining invites"),
                () -> assertTrue(page1.getLast(), "last should be true")
        );
    }

    @Test
    @DisplayName("Исходящие приглашения: корректный поиск по searchQuery")
    @User(outcomeInvitation = 2)
    void outcomeInvitationsAreListedAndFilteredWhenSearchQueryProvided(AppUser user) {
        final String inviteeUsername = AnnotationHelper.outgoingInviteeUsername(user);

        UsersPageResponse filtered = userdataBlockingStub.outcomeInvitations(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(10)
                        .setSearchQuery(inviteeUsername)
                        .build()
        );
        assertAll("Outcome filtered",
                () -> assertTrue(filtered.getContentCount() >= 1, "filtered list should not be empty"),
                () -> assertTrue(filtered.getContentList().stream().allMatch(u -> u.getUsername().contains(inviteeUsername)),
                        "all items should match search query")
        );
    }
}
