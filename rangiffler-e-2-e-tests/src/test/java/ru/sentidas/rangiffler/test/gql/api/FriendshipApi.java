package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import io.qameta.allure.Step;
import ru.sentidas.FriendshipActionMutation;
import ru.sentidas.type.FriendshipAction;
import ru.sentidas.type.FriendshipInput;

public class FriendshipApi {

    private final ApolloClient apollo;

    public FriendshipApi(ApolloClient apollo) {
        this.apollo = apollo;
    }

    @Step("GQL FriendshipAction: SEND_INVITATION to userId={userId}")
    public FriendshipActionMutation.Data sentInvitation(String token, String userId) {
        return action(token, userId, FriendshipAction.ADD);
    }

    @Step("GQL FriendshipAction: ACCEPT from userId={userId}")
    public FriendshipActionMutation.Data accept(String token, String userId) {
        return action(token, userId, FriendshipAction.ACCEPT);
    }

    @Step("GQL FriendshipAction: REJECT from userId={userId}")
    public FriendshipActionMutation.Data reject(String token, String userId) {
        return action(token, userId, FriendshipAction.REJECT);
    }

    @Step("GQL FriendshipAction: DELETE friend userId={userId}")
    public FriendshipActionMutation.Data deleteFriend(String token, String userId) {
        return action(token, userId, FriendshipAction.DELETE);
    }

    @Step("GQL FriendshipAction without auth: action={action}, userId={userId}")
    public ApolloResponse<FriendshipActionMutation.Data> tryActionWithoutAuth(String userId, FriendshipAction action) {
        FriendshipActionMutation mutation = FriendshipActionMutation.builder()
                .input(FriendshipInput.builder().user(userId).action(action).build())
                .build();

        ApolloCall<FriendshipActionMutation.Data> call = apollo.mutation(mutation);
        return Rx2Apollo.single(call).blockingGet();
    }

    @Step("GQL FriendshipAction raw: action={action}, userId={userId}")
    public ApolloResponse<FriendshipActionMutation.Data> tryAction(String token, String userId, FriendshipAction action) {
        FriendshipActionMutation mutation = FriendshipActionMutation.builder()
                .input(FriendshipInput.builder().user(userId).action(action).build())
                .build();

        ApolloCall<FriendshipActionMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);
        return Rx2Apollo.single(call).blockingGet();
    }

    private FriendshipActionMutation.Data action(String token, String userId, FriendshipAction action) {
        FriendshipActionMutation mutation = FriendshipActionMutation.builder()
                .input(FriendshipInput.builder().user(userId).action(action).build())
                .build();

        ApolloCall<FriendshipActionMutation.Data> call = apollo.mutation(mutation)
                .addHttpHeader("authorization", token);

        ApolloResponse<FriendshipActionMutation.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }
}
