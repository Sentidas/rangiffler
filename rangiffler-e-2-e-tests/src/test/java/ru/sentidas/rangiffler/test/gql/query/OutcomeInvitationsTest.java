package ru.sentidas.rangiffler.test.gql.query;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sentidas.GetOutcomeInvitationsQuery;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.test.gql.BaseGraphQlTest;
import ru.sentidas.rangiffler.test.gql.api.UserApi;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GQL_Исходящие приглашения")
public class OutcomeInvitationsTest extends BaseGraphQlTest {

    private final UserApi userApi = new UserApi(apolloClient);

    @Test
    @User(outcomeInvitation = 2)
    @ApiLogin
    @DisplayName("GetOutcomeInvitations: корректные поля исходящих приглашений")
    void getOutcomeInvitationsMustReturnCorrectFields(@Token String bearerToken) {
        GetOutcomeInvitationsQuery.Data response = userApi.getOutcomeInvitations(bearerToken, 0, 10);

        assertAll("outcome invitations",
                () -> assertNotNull(response.user.outcomeInvitations.edges, "edges must not be null"),
                () -> assertTrue(response.user.outcomeInvitations.edges.size() > 0, "must not be empty")
        );

        int index = 0;
        for (GetOutcomeInvitationsQuery.Edge edge : response.user.outcomeInvitations.edges) {
            GetOutcomeInvitationsQuery.Node receiver = edge.node;
            int i = index++;
            assertAll("receiver node #" + i,
                    () -> assertNotNull(receiver.id, "id must not be null"),
                    () -> assertFalse(receiver.username.isEmpty(), "username must not be empty"),
                    () -> assertNotNull(receiver.friendStatus, "friendStatus must not be null")
            );
        }
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("GetOutcomeInvitations: пустой список, если нет исходящих приглашений")
    void getOutcomeInvitationsMustReturnEmptyWhenNoInvitations(@Token String bearerToken) {
        GetOutcomeInvitationsQuery.Data response = userApi.getOutcomeInvitations(bearerToken, 0, 10);
        assertEquals(0, response.user.outcomeInvitations.edges.size());
    }
}
