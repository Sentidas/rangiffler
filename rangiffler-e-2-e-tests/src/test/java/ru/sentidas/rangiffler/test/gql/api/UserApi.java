package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import ru.sentidas.*;
import ru.sentidas.type.CountryInput;
import ru.sentidas.type.UserInput;

public class UserApi {

    private final ApolloClient apolloClient;

    public UserApi(ApolloClient apollo) {
        this.apolloClient = apollo;
    }

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


    public GetUserQuery.Data getUser(String token) {
        GetUserQuery query = GetUserQuery.builder().build();
        ApolloCall<GetUserQuery.Data> call = apolloClient.query(query)
                .addHttpHeader("authorization", token);

        ApolloResponse<GetUserQuery.Data> resp = Rx2Apollo.single(call).blockingGet();
        return resp.dataOrThrow();
    }

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

    public GetIncomeInvitationsQuery.Data getInvitations(String bearerToken, int page, int size) {
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
