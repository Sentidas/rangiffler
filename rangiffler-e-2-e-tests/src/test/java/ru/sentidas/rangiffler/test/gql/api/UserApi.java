package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import ru.sentidas.GetUserQuery;
import ru.sentidas.UpdateUserMutation;
import ru.sentidas.type.CountryInput;
import ru.sentidas.type.UserInput;

public class UserApi {

    private final ApolloClient apollo;

    public UserApi(ApolloClient apollo) {
        this.apollo = apollo;
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

        ApolloCall<UpdateUserMutation.Data> call = apollo.mutation(mutation)
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

        ApolloCall<UpdateUserMutation.Data> call = apollo.mutation(mutation);
        return Rx2Apollo.single(call).blockingGet();
    }


    public GetUserQuery.Data getUser(String token) {
        GetUserQuery query = GetUserQuery.builder().build();
        ApolloCall<GetUserQuery.Data> call = apollo.query(query)
                .addHttpHeader("authorization", token);

        ApolloResponse<GetUserQuery.Data> resp = Rx2Apollo.single(call).blockingGet();
        return resp.dataOrThrow();
    }
}
