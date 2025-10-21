package ru.sentidas.rangiffler.test.grpc.userdata;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.rangiffler.grpc.UserPageRequest;
import ru.sentidas.rangiffler.grpc.UsersPageResponse;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GrpcTest;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.grpc.BaseTest;

import static org.junit.jupiter.api.Assertions.*;

@GrpcTest
@DisplayName("Userdata: allFriendsPage")
public class AllFriendsTest extends BaseTest {

    @Test
    @DisplayName("Список друзей: корректная пагинация, когда друзей больше размера страницы")
    @User(friends = 3)
    void allFriendsPagePaginatedCorrectlyWhenMoreThanPageSize(AppUser user) {
        final UsersPageResponse page0 = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(0)
                        .setSize(2)
                        .build()
        );

        assertAll("Slice page 0",
                () -> assertEquals(2, page0.getSize(), "size should be 2 on page 0"),
                () -> assertEquals(0, page0.getPage(), "page index should be 0"),
                () -> assertTrue(page0.getFirst(), "first should be true"),
                () -> assertFalse(page0.getLast(), "last should be false when hasNext")
        );

        UsersPageResponse page1 = userdataBlockingStub.allFriendsPage(
                UserPageRequest.newBuilder()
                        .setUsername(user.username())
                        .setPage(1)
                        .setSize(2)
                        .build()
        );

        assertAll("Slice page 1",
                () -> assertEquals(2, page1.getSize(), "size should be 2 on page 1"),
                () -> assertEquals(1, page1.getPage(), "page index should be 1"),
                () -> assertFalse(page1.getFirst(), "first should be false on page 1"),
                () -> assertTrue(page1.getLast(), "last should be true on final slice page")
        );
    }
}
