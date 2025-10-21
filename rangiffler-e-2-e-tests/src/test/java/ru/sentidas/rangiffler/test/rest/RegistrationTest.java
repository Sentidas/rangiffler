package ru.sentidas.rangiffler.test.rest;

import okhttp3.ResponseBody;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.utils.AnnotationHelper;

import static org.junit.jupiter.api.Assertions.*;
import static ru.sentidas.rangiffler.utils.AnnotationHelper.getUniqueTestUsername;

@DisplayName("Rest_Регистрация")
class RegistrationTest extends BaseAuthTest {

    private final UsersDbClient usersDbClient = new UsersDbClient();
    private static final String defaultPassword = "12345";

    @Test
    @DisplayName("Успешная регистрация: 201 и показ страницы подтверждения")
    void createUserWhenRegistrationDataIsValid() throws Exception {
        // 1) GET /register → XSRF-TOKEN
        Response<ResponseBody> registerFormResponse = authClientFollowRedirect.requestRegisterForm();

        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");

        assertAll("registerResponse registerFormResponse (GET /registerResponse)",
                () -> assertEquals(200, registerFormResponse.code(), "Register registerFormResponse should return 200"),
                () -> assertNotNull(csrf, "XSRF-TOKEN must be present after GET /registerResponse"),
                () -> assertFalse(csrf.isEmpty(), "XSRF-TOKEN must not be empty")
        );

        // 2) POST /register c XSRF-TOKEN
        final String username = getUniqueTestUsername();
        Response<ResponseBody> registerResponse = authClientFollowRedirect.register(
                username,
                "12345",
                "12345",
                csrf
        );

        assertAll("registration",
                () -> assertEquals(201, registerResponse.code(), "GET /login?error should return 200"),
                () -> assertEquals("text/html;charset=UTF-8", registerResponse.headers().get("Content-Type")),
                () -> assertTrue(registerResponse.body().string().contains("Congratulations! You've registered!"))
        );

        final AppUser userExists = usersDbClient.findUserByUsername(username);

        assertAll("the user exists",
                () -> assertNotNull(userExists.id()),
                () -> assertEquals(username, userExists.username())
        );
    }

    @Test
    @User
    @DisplayName("Неуспешная регистрация: username уже занят — 400 и сообщение об ошибке")
    void returnBadRequestWhenUsernameAlreadyExists(AppUser user) throws Exception {
        // 1) GET /register →  XSRF-TOKEN
        authClientFollowRedirect.requestRegisterForm();
        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");

        // 2) POST /register с ошибкой
        Response<ResponseBody> registrationErrorResponse = authClientFollowRedirect.register(
                user.username(),
                defaultPassword,
                defaultPassword,
                csrf
        );

        assertAll("registration error (duplicate username)",
                () -> assertEquals(400, registrationErrorResponse.code(), "Post /register error should return 400"),
                () -> assertEquals("text/html;charset=UTF-8", registrationErrorResponse.headers().get("Content-Type")),
                () -> assertTrue(registrationErrorResponse.errorBody().string().contains("Username `" + user.username() + "` already exists"),
                        "error page must contain duplicate username message")
        );
    }

    @Test
    @DisplayName("Неуспешная регистрация: имя слишком короткое — 400 и сообщение об ошибке")
    void returnBadRequestWhenUsernameTooShort() throws Exception {
        // 1) GET /register → XSRF-TOKEN
        authClientFollowRedirect.requestRegisterForm();
        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");

        // 2) POST /register с ошибкой
        Response<ResponseBody> registrationErrorResponse = authClientFollowRedirect.register(
                "Ян",
                defaultPassword,
                defaultPassword,
                csrf
        );

        assertAll("registration error (short username)",
                () -> assertEquals(400, registrationErrorResponse.code(), "Post /register error should return 400"),
                () -> assertEquals("text/html;charset=UTF-8", registrationErrorResponse.headers().get("Content-Type")),
                () -> assertTrue(registrationErrorResponse.errorBody().string().contains("Allowed username length should be from 3 to 50 characters"),
                        "error page must contain username length message")
        );
    }

    @Test
    @DisplayName("Неуспешная регистрация: пароли не совпадают — 400 и сообщение об ошибке")
    void returnBadRequestWhenPasswordsDoNotMatch() throws Exception {
        // 1) GET /register →  XSRF-TOKEN
        authClientFollowRedirect.requestRegisterForm();
        final String csrf = ThreadSafeCookieStore.INSTANCE.cookieValue("XSRF-TOKEN");

        // 2) POST /register с ошибкой
        Response<ResponseBody> registrationErrorResponse = authClientFollowRedirect.register(
                AnnotationHelper.getUniqueTestUsername(),
                "1234",
                defaultPassword,
                csrf
        );

        assertAll("registration error (password mismatch)",
                () -> assertEquals(400, registrationErrorResponse.code(), "Post /register error should return 400"),
                () -> assertEquals("text/html;charset=UTF-8", registrationErrorResponse.headers().get("Content-Type")),
                () -> assertTrue(registrationErrorResponse.errorBody().string().contains("Passwords should be equal"),
                        "error page must contain password mismatch message")
        );
    }
}
