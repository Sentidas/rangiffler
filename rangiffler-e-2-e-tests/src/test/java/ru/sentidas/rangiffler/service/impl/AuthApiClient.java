package ru.sentidas.rangiffler.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Response;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;
import ru.sentidas.rangiffler.rest.AuthApi;
import ru.sentidas.rangiffler.rest.core.CodeInterceptor;
import ru.sentidas.rangiffler.rest.core.RestClient;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.utils.OAuthUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;

@ParametersAreNonnullByDefault
public class AuthApiClient extends RestClient {

    private static final Config CFG = Config.getInstance();
    private final AuthApi authApi;
    private final CodeInterceptor codeInterceptor;

    public AuthApiClient() {
        this(new CodeInterceptor(), true);
    }

    public AuthApiClient(boolean followRedirect) {
        this(new CodeInterceptor(), followRedirect);
    }

    public AuthApiClient(CodeInterceptor codeInterceptor, boolean followRedirect) {
        super(CFG.authUrl(), followRedirect, codeInterceptor);
        this.codeInterceptor = codeInterceptor;
        this.authApi = create(AuthApi.class);
    }

    @SneakyThrows
    @NotNull
    public String login(String username, String password) {
        final String codeVerifier = OAuthUtils.generateCodeVerifier();
        final String codeChallenge = OAuthUtils.generateCodeChallange(codeVerifier);
        final String redirectUri = CFG.frontUrl() + "authorized";
        final String clientId = "client";

        // 1) authorize (нужен XSRF-TOKEN и редирект с code)
        authApi.authorize(
                "code",
                clientId,
                "openid",
                redirectUri,
                codeChallenge,
                "S256"
        ).execute();

        // 2) login с XSRF-TOKEN
        authApi.login(
                username,
                password,
                ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
        ).execute();

        // 3) token по полученному authorization_code
        Response<JsonNode> tokenResponse = authApi.token(
                ApiLoginExtension.getCode(),
                redirectUri,
                clientId,
                codeVerifier,
                "authorization_code"
        ).execute();

        return tokenResponse.body().get("id_token").asText();
    }

    @NotNull
    public String getAuthorizationCode() {
        return codeInterceptor.getLastCode();
    }

    // === STEP-методы для пошаговых тестов ===

    public Response<Void> authorize(String responseType,
                                    String clientId,
                                    String scope,
                                    String redirectUri,
                                    String codeChallenge,
                                    String codeChallengeMethod) throws Exception {
        Call<Void> call = authApi.authorize(
                responseType, clientId, scope, redirectUri, codeChallenge, codeChallengeMethod
        );
        return call.execute();
    }

    @NotNull
    public Response<Void> loginStep(String username, String password, String csrf) throws Exception {
        Call<Void> call = authApi.login(username, password, csrf);
        return call.execute();
    }

    @NotNull
    public Response<ResponseBody> loginError() throws Exception {
        return authApi.requestLoginForm().execute();
    }

    @NotNull
    public Response<JsonNode> token(String code,
                                    String redirectUri,
                                    String clientId,
                                    String codeVerifier,
                                    String grantType) throws Exception {
        Call<JsonNode> call = authApi.token(code, redirectUri, clientId, codeVerifier, grantType);
        return call.execute();
    }

    @NotNull
    public Response<ResponseBody> register(String username,
                                           String password,
                                           String passwordSubmit,
                                           String _csrf) throws IOException {
        Call<ResponseBody> call = authApi.register(username, password, passwordSubmit, _csrf);
        return call.execute();
    }

    @NotNull
    public Response<ResponseBody> requestRegisterForm() throws Exception {
        return authApi.requestRegisterForm().execute();
    }
}