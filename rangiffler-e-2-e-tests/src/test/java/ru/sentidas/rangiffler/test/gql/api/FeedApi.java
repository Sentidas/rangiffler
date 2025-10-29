package ru.sentidas.rangiffler.test.gql.api;

import com.apollographql.apollo.api.ApolloResponse;
import com.apollographql.java.client.ApolloCall;
import com.apollographql.java.client.ApolloClient;
import com.apollographql.java.rx2.Rx2Apollo;
import io.qameta.allure.Step;
import ru.sentidas.GetFeedQuery;

import javax.annotation.Nonnull;


public class FeedApi {

    private final ApolloClient apollo;

    public FeedApi(ApolloClient apollo) {
        this.apollo = apollo;
    }

    @Nonnull
    //@Step("GQL GetFeed: page={page}, size={size}, withFriends={withFriends}")
    public GetFeedQuery.Data getFeed(String token, int page, int size, boolean withFriends) {
        GetFeedQuery query = GetFeedQuery.builder()
                .page(page)
                .size(size)
                .withFriends(withFriends)
                .build();

        ApolloCall<GetFeedQuery.Data> call = apollo.query(query)
                .addHttpHeader("authorization", token);

        ApolloResponse<GetFeedQuery.Data> response = Rx2Apollo.single(call).blockingGet();
        return response.dataOrThrow();
    }

    @Nonnull
    @Step("GQL GetFeed without auth: page={page}, size={size}, withFriends={withFriends}")
    public ApolloResponse<GetFeedQuery.Data> tryGetFeedWithoutAuth(int page, int size, boolean withFriends) {
        GetFeedQuery query = GetFeedQuery.builder()
                .page(page)
                .size(size)
                .withFriends(withFriends)
                .build();

        ApolloCall<GetFeedQuery.Data> call = apollo.query(query);
        return Rx2Apollo.single(call).blockingGet();
    }
}
