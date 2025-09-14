package ru.sentidas.rangiffler.service;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.SneakyThrows;
import retrofit2.Response;
import ru.sentidas.rangiffler.api.AuthApi;
import ru.sentidas.rangiffler.api.core.CodeInterceptor;
import ru.sentidas.rangiffler.api.core.RestClient;
import ru.sentidas.rangiffler.api.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;
import ru.sentidas.rangiffler.utils.OAuthUtils;


public class AuthApiClient extends RestClient {

  private static final Config CFG = Config.getInstance();
  private final AuthApi authApi;

  public AuthApiClient() {
    super(CFG.authUrl(), true, new CodeInterceptor());
    this.authApi = create(AuthApi.class);
  }

  @SneakyThrows
  public String login(String username, String password) {
    final String codeVerifier = OAuthUtils.generateCodeVerifier();
    final String codeChallenge = OAuthUtils.generateCodeChallange(codeVerifier);
    final String redirectUri = CFG.frontUrl() + "authorized";
    final String clientId = "client";

    authApi.authorize(
        "code",
        clientId,
        "openid",
        redirectUri,
        codeChallenge,
        "S256"
    ).execute();

    authApi.login(
        username,
        password,
        ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN")
    ).execute();

    Response<JsonNode> tokenResponse = authApi.token(
        ApiLoginExtension.getCode(),
        redirectUri,
        clientId,
        codeVerifier,
        "authorization_code"
    ).execute();

    return tokenResponse.body().get("id_token").asText();
  }
}
