package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import io.qameta.allure.Step;
import ru.sentidas.*;
import ru.sentidas.type.CountryInput;
import ru.sentidas.type.UserInput;

public class UserApi {

    private final ApolloClient apolloClient;

    public UserApi(ApolloClient apollo) {
        this.apolloClient = apollo;
    }

    @Step("GQL UpdateUser: firstname={firstname}, surname={surname}, country={countryCode}")
    public UpdateUserMutation.Data updateUser(String token,
                                              String firstname,
                                              String surname,
                                              String avatarDataUrl,
                                              String countryCode) {

        UserInput.Builder u = UserInput.builder();
        if (firstname != null) u.firstname(firstname);
        if (surname != null) u.surname(surname);
        if (avatarDataUrl != null) u.avatar(avatarDataUrl);
        if (countryCode != null) u.location(CountryInput.builder().code(countryCode).build());

        UpdateUserMutation mutation = UpdateUserMutation.builder()
                .input(u.build())
                .build();

        ApolloCall<UpdateUserMutation.Data> call = apolloClient.mutation(mutation)
                .addHttpHeader("authorization", token);

        ApolloResponse<UpdateUserMutation.Data> resp = Rx2Apollo.single(call).blockingGet();
        return resp.dataOrThrow();
    }

    @Step("GQL UpdateUser without auth: firstname={firstname}, surname={surname}, country={countryCode}")
    public ApolloResponse<UpdateUserMutation.Data> tryUpdateUserWithoutAuth(String firstname,
                                                                            String surname,
                                                                            String avatarDataUrl,
                                                                            String countryCode) {
        UserInput.Builder u = UserInput.builder();
        if (firstname != null) u.firstname(firstname);
        if (surname != null) u.surname(surname);
        if (avatarDataUrl != null) u.avatar(avatarDataUrl);
        if (countryCode != null) u.location(CountryInput.builder().code(countryCode).build());

        UpdateUserMutation mutation = UpdateUserMutation.builder()
                .input(u.build())
                .build();

        ApolloCall<UpdateUserMutation.Data> call = apolloClient.mutation(mutation);
        return Rx2Apollo.single(call).blockingGet();
    }

    @Step("GQL GetUser")
    public GetUserQuery.Data getUser(String token) {
        GetUserQuery query = GetUserQuery.builder().build();
        ApolloCall<GetUserQuery.Data> call = apolloClient.query(query)
                .addHttpHeader("authorization", token);

        ApolloResponse<GetUserQuery.Data> resp = Rx2Apollo.single(call).blockingGet();
        return resp.dataOrThrow();
    }

    @Step("GQL GetOutcomeInvitations: page={page}, size={size}")
    public GetOutcomeInvitationsQuery.Data getOutcomeInvitations(String bearerToken, int page, int size) {
        GetOutcomeInvitationsQuery query = GetOutcomeInvitationsQuery.builder()
                .page(page)
                .size(size)
                .searchQuery(null)
                .build();
        ApolloCall<GetOutcomeInvitationsQuery.Data> call = apolloClient.query(query)
                .addHttpHeader("authorization", bearerToken);
        ApolloResponse<GetOutcomeInvitationsQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    @Step("GQL GetIncomeInvitations: page={page}, size={size}")
    public GetIncomeInvitationsQuery.Data getIncomeInvitations(String bearerToken, int page, int size) {
        GetIncomeInvitationsQuery query = GetIncomeInvitationsQuery.builder()
                .page(page)
                .size(size)
                .searchQuery(null)
                .build();
        ApolloCall<GetIncomeInvitationsQuery.Data> call = apolloClient.query(query)
                .addHttpHeader("authorization", bearerToken);
        ApolloResponse<GetIncomeInvitationsQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    @Step("GQL GetFriends: page={page}, size={size}")
    public GetFriendsQuery.Data getFriends(String bearerToken, int page, int size) {
        GetFriendsQuery query = GetFriendsQuery.builder()
                .page(page)
                .size(size)
                .searchQuery(null)
                .build();
        ApolloCall<GetFriendsQuery.Data> call = apolloClient.query(query)
                .addHttpHeader("authorization", bearerToken);
        ApolloResponse<GetFriendsQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

}
