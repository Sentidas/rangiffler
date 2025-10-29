package ru.sentidas.rangiffler.test.gql.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetIncomeInvitationsQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UserApi;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GQL_Входящие приглашения")
public class IncomeInvitationsTest extends BaseGraphQlTest {

    private final UserApi userApi = new UserApi(apolloClient);

    @Test
    @User(incomeInvitation = 2)
    @ApiLogin
    @DisplayName("GetInvitations: корректные поля входящих приглашений")
    void getInvitationsMustReturnCorrectFields(@Token String bearerToken) {
        GetIncomeInvitationsQuery.Data response = userApi.getIncomeInvitations(bearerToken, 0, 10);

        assertAll("income invitations",
                () -> assertNotNull(response.user.incomeInvitations.edges, "edges must not be null"),
                () -> assertTrue(response.user.incomeInvitations.edges.size() > 0, "must not be empty")
        );

        int index = 0;
        for (GetIncomeInvitationsQuery.Edge edge : response.user.incomeInvitations.edges) {
            GetIncomeInvitationsQuery.Node inviter = edge.node;
            int i = index++;
            assertAll("inviter node #" + i,
                    () -> assertNotNull(inviter.id, "id must not be null"),
                    () -> assertFalse(inviter.username.isEmpty(), "username must not be empty"),
                    () -> assertNotNull(inviter.friendStatus, "friendStatus must not be null")
            );
        }
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("GetInvitations: пустой список, если нет входящих приглашений")
    void getInvitationsMustReturnEmptyWhenNoInvitations(@Token String bearerToken) {
        GetIncomeInvitationsQuery.Data response = userApi.getIncomeInvitations(bearerToken, 0, 10);
        assertEquals(0, response.user.incomeInvitations.edges.size());
    }
}
