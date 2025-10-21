package ru.sentidas.rangiffler.test.gql;

import com.apollographql.adapter.core.DateAdapter;
import com.apollographql.java.client.ApolloClient;
import io.qameta.allure.okhttp3.AllureOkHttp3;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.GqlTest;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;
import ru.sentidas.type.Date;

@GqlTest
public class BaseGraphQlTest {

    protected static Config CFG = Config.getInstance();

    @RegisterExtension
    protected static final ApiLoginExtension apiLoginExtension = ApiLoginExtension.rest();

    protected static final ApolloClient apolloClient = new ApolloClient.Builder()
            .serverUrl(CFG.gatewayUrl() + "graphql")
            .addCustomScalarAdapter(Date.type, DateAdapter.INSTANCE)
            .okHttpClient(
                    new OkHttpClient.Builder()
                            .addNetworkInterceptor(new AllureOkHttp3())
                            .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                            .build()
            ).build();
}
