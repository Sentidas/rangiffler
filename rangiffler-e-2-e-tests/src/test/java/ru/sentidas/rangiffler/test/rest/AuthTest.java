package ru.sentidas.rangiffler.test.rest;

import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
import ru.sentidas.rangiffler.jupiter.annotaion.Token;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.rest.model.Code;
import ru.sentidas.rangiffler.rest.model.jwt.JwtToken;

import java.time.Instant;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Rest_Авторизация")
public class AuthTest extends BaseAuthTest {

    private final String authUrl = CFG.authUrl();
    final String clientId = "client";
    final String redirectUri = CFG.frontUrl() + "authorized";

    @Test
    @User
    @DisplayName("Успешный PKCE-флоу → выдаётся валидный id_token (JWT)")
    void issueIdTokenWhenPkceFlowIsValid(AppUser user) throws Exception {
        final Code pkce = Code.generate();

        // 1. AUTHORIZE
        Response<Void> authorizeRes = authClientFollowRedirect.authorize(
                "code",
                clientId,
                "openid",
                redirectUri, pkce.codeChallenge(),
                "S256"
        );

        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");
        assertAll("authorize",
                () -> assertTrue(authorizeRes.isSuccessful(), "authorize: expected 2xx"),
                () -> assertNotNull(csrf, "XSRF-TOKEN must be present"),
                () -> assertFalse(csrf.isEmpty(), "XSRF-TOKEN must not be empty")
        );

        //  2. LOGIN
        Response<Void> loginRes = authClientFollowRedirect.loginStep(
                user.username(),
                user.testData().password(),
                csrf);


        final int loginStatusCode = loginRes.code();
        assertTrue(loginRes.isSuccessful(), "login: expected 2xx when following redirects");
        assertEquals(200, loginStatusCode, "login: expected 200 when following redirects");

        // 3: auth code
        final String code = authClientFollowRedirect.getAuthorizationCode();

        assertAll("authorization-code",
                () -> assertNotNull(code, "authorization code must be present"),
                () -> assertFalse(code.isEmpty(), "authorization code must not be empty")
        );

        // 4: TOKEN — change auth code on id_token
        Response<JsonNode> tokenRes = authClientFollowRedirect.token(
                code, redirectUri, clientId, pkce.codeVerifier(), "authorization_code"
        );

        assertAll("token",
                () -> assertTrue(tokenRes.isSuccessful(), "token: expected 200"),
                () -> assertNotNull(tokenRes.body(), "token: body must not be null"),
                () -> assertTrue(tokenRes.body().has("id_token"), "token: 'id_token' must be present")
        );

        final String idToken = tokenRes.body().get("id_token").asText();

        assertAll("id-token",
                () -> assertNotNull(idToken, "id_token must not be null"),
                () -> assertFalse(idToken.isEmpty(), "id_token must not be empty"),
                () -> assertEquals(3, idToken.split("\\.").length, "id_token must be in JWT format (3 parts)")
        );
    }

    @Test
    @User
    @ApiLogin
    @DisplayName("JWT: заголовок, полезная нагрузка и подпись соответствуют контракту")
    void jwtHeaderPayloadSignatureMatchContract(@Token String idToken, AppUser user) {
        final JwtToken token = JwtToken.parse(idToken);
        final String expectedIssuer = authUrl.endsWith("/")
                ? authUrl.substring(0, authUrl.length() - 1)
                : authUrl;

        // === HEADER ===
        assertAll("header",
                () -> assertEquals("RS256", token.header().algorithm(), "alg must be RS256"),
                () -> assertNotNull(token.header().keyId(), "kid must be present"),
                () -> assertFalse(token.header().keyId().isBlank(), "kid must not be blank")
        );

        // === PAYLOAD ===
        assertAll("payload",
                () -> assertEquals(expectedIssuer, token.payload().issuer(), "iss must point to current auth server origin"),
                () -> assertEquals("client", token.payload().audience(), "aud must equal client_id"),
                () -> assertEquals("client", token.payload().authorizedParty(), "azp must equal client_id"),
                () -> assertEquals(user.username(), token.payload().subject(), "sub (username) must match test user"),
                () -> assertNotNull(token.payload().jwtId(), "jti must be present"),
                () -> assertFalse(token.payload().jwtId().isBlank(), "jti must not be blank"),
                () -> assertNotNull(token.payload().sessionId(), "sid must be present"),
                () -> assertFalse(token.payload().sessionId().isBlank(), "sid must not be blank")
        );

        // === TIME ===
        final long now = Instant.now().getEpochSecond();
        final long leeway = 10;
        final long iat = token.payload().issuedAt();
        final long exp = token.payload().expiresAt();
        final long authTime = token.payload().authTime();

        assertAll("time",
                () -> assertTrue(iat <= now + leeway, "iat must not be in the future (± leeway)"),
                () -> assertTrue(authTime <= now + leeway, "auth_time must not be in the future (± leeway)"),
                () -> assertTrue(exp > now - leeway, "exp must be in the future (± leeway)"),
                () -> assertTrue(authTime <= iat, "auth_time must be <= iat"),
                () -> assertTrue(exp > iat, "exp must be > iat"),
                () -> assertEquals(1800, exp - iat, "TTL must be exactly 1800 seconds (30 minutes)")
        );

        // === SIGNATURE ===
        assertAll("signature",
                () -> assertNotNull(token.signature(), "signature must be present"),
                () -> assertFalse(token.signature().isBlank(), "signature must not be blank"),
                () -> assertDoesNotThrow(() -> Base64.getUrlDecoder().decode(token.signature()),
                        "signature must be valid base64url")
        );
    }

    @Test
    @DisplayName("Неверный логин: редирект на /login?error и страница содержит текст ошибки")
    void redirectToLoginErrorWhenCredentialsInvalid() throws Exception {
        final Code pkce = Code.generate();

        // authorize с (редирект ON) для получения XSRF-TOKEN в cookies
        authClientFollowRedirect.authorize(
                "code",
                clientId,
                "openid",
                redirectUri,
                pkce.codeChallenge(),
                "S256"
        );

        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");

        // POST/login (редирект OFF) с неверными данными → редирект на /login?error
        Response<Void> loginResp = authClientNoRedirect.loginStep(
                "unsuccessful_login",
                "12345",
                csrf);

        final int loginStatus = loginResp.code();
        final String location = loginResp.headers().get("Location");

        assertAll("login redirect",
                () -> assertEquals(302, loginStatus, "Login with error credential should return redirect (302)"),
                () -> assertNotNull(location, "Login response with error credential must contain Location header"),
                () -> assertTrue(location.endsWith("/login?error") || location.contains("/login?error"),
                        () -> "Location must point to /login?error, actual: " + location)
        );

        // GET /login?error → 200
        Response<ResponseBody> error = authClientNoRedirect.loginError();
        assertAll("login error",
                () -> assertEquals(200, error.code(), "GET /login?error should return 200"),
                () -> assertEquals("text/html;charset=UTF-8", error.headers().get("Content-Type")),
                () -> assertTrue(error.body().string().contains("Неверные учетные данные пользователя"))
        );

        assertNull(authClientNoRedirect.getAuthorizationCode(),
                "authorization code must not be issued when credentials are invalid");
    }
}







