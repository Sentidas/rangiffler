
package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import io.qameta.allure.Step;
import ru.sentidas.GetFriendsOfFriendsQuery;
import ru.sentidas.GetPeopleQuery;


public class UsersApi {

    private final ApolloClient apollo;

    public UsersApi(ApolloClient apollo) {
        this.apollo = apollo;
    }

    @Step("GQL GetPeople: page={page}, size={size}, search={searchQuery}")
    public GetPeopleQuery.Data users(String token, int page, int size, String searchQuery) {
        GetPeopleQuery query = GetPeopleQuery.builder()
                .page(page)
                .size(size)
                .searchQuery(searchQuery)
                .build();

        ApolloCall<GetPeopleQuery.Data> call = apollo.query(query)
                .addHttpHeader("authorization", token);

        ApolloResponse<GetPeopleQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    @Step("GQL GetFriendsOfFriends")
    public ApolloResponse<GetFriendsOfFriendsQuery.Data> getFriendsOfFriends(String token) {
        GetFriendsOfFriendsQuery query = GetFriendsOfFriendsQuery.builder().build();

        ApolloCall<GetFriendsOfFriendsQuery.Data> call = apollo.query(query)
                .addHttpHeader("Authorization", token);
        return Rx2Apollo.single(call).blockingGet();
    }
}
