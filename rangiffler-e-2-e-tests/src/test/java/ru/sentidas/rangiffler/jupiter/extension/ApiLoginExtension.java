//package ru.sentidas.rangiffler.jupiter.extension;
//
//import com.codeborne.selenide.Selenide;
//import com.codeborne.selenide.WebDriverRunner;
//
//import guru.qa.niffler.page.pages.MainPage;
//import guru.qa.niffler.service.impl.AuthApiClient;
//import org.junit.jupiter.api.extension.*;
//import org.junit.platform.commons.support.AnnotationSupport;
//import org.openqa.selenium.Cookie;
//import ru.sentidas.rangiffler.api.core.ThreadSafeCookieStore;
//import ru.sentidas.rangiffler.config.Config;
//import ru.sentidas.rangiffler.jupiter.annotaion.ApiLogin;
//import ru.sentidas.rangiffler.model.Photo;
//import ru.sentidas.rangiffler.model.TestData;
//import ru.sentidas.rangiffler.model.User;
//import ru.sentidas.rangiffler.service.UsersDbClient;
//
//import javax.annotation.ParametersAreNonnullByDefault;
//import java.util.List;
//
//
//@ParametersAreNonnullByDefault
//public class ApiLoginExtension implements BeforeEachCallback, ParameterResolver {
//
//    private static final Config CFG = Config.getInstance();
//    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(ApiLoginExtension.class);
//
//    private final AuthApiClient authApiClient = new AuthApiClient();
//
//    private final UsersDbClient usersApiClient = new UsersDbClient();
//
//    private final boolean setupBrowser;
//
//    private ApiLoginExtension(boolean setupBrowser) {
//        this.setupBrowser = setupBrowser;
//    }
//
//    public ApiLoginExtension() {
//        this.setupBrowser = true;
//    }
//
//    public static ApiLoginExtension rest() {
//        return new ApiLoginExtension(false);
//    }
//
//    @Override
//    public void beforeEach(ExtensionContext context) throws Exception {
//        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ApiLogin.class)
//                .ifPresent(apiLogin -> {
//
//                    final UserJson userToLogin;
//                    final UserJson userFromUserExtension = UserExtension.createdUser();
//
//
//                    if ("".equals(apiLogin.username()) || "".equals(apiLogin.password())) {
//                        if (userFromUserExtension == null) {
//                            throw new IllegalStateException("@User must be present in case that @ApiLogin is empty!");
//                        }
//                        userToLogin = userFromUserExtension;
//                    } else {
//                        String username = apiLogin.username();
//                        String password = apiLogin.password();
//
//                     //   List<Photo> categories = spendApiClient.existingCategories(username);
//
//                        List<User> friendsList = usersApiClient.getFriends(username);
//                        List<User> peopleList = usersApiClient.getAllUsers(username);
//
//                        List<User> friends = friendsList.stream()
//                                .filter(userJson -> FriendshipStatus.FRIEND.equals(userJson.friendshipStatus()))
//                                .toList();
//                        List<User> incomeInvitations = friendsList.stream()
//                                .filter(userJson -> FriendshipStatus.INVITE_RECEIVED.equals(userJson.friendshipStatus()))
//                                .toList();
//                        List<User> outcomeInvitations = peopleList.stream()
//                                .filter(userJson -> FriendshipStatus.INVITE_SENT. equals(userJson.friendshipStatus()))
//                                .toList();
//
//                        System.out.println("исходящие приглашения в аннотации: " + outcomeInvitations);
//
//                        UserJson fakeUser = new UserJson(
//                                username,
//                                new TestData(
//                                        password,
//                                        categories,
//                                        spends,
//                                        friends,
//                                        incomeInvitations,
//                                        outcomeInvitations
//                                )
//                        );
//                        if (userFromUserExtension != null) {
//                            throw new IllegalStateException("@User must not be present in case that @ApiLogin contains username or password!");
//                        }
//                        UserExtension.setUser(fakeUser);
//                        userToLogin = fakeUser;
//                    }
//
//                    final String token = authApiClient.login(
//                            userToLogin.username(),
//                            userToLogin.testData().password()
//                    );
//                    setToken(token);
//                    if (setupBrowser) {
//                        Selenide.open(CFG.frontUrl());
//                        Selenide.localStorage().setItem("id_token", getToken());
//                        WebDriverRunner.getWebDriver().manage().addCookie(
//                                getJsessionIdCookie()
//                        );
//                        Selenide.open(MainPage.URL, MainPage.class).checkThatPageLoaded();
//                    }
//                });
//    }
//
//    @Override
//    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        return parameterContext.getParameter().getType().isAssignableFrom(String.class)
//                && AnnotationSupport.isAnnotated(parameterContext.getParameter(), Token.class);
//    }
//
//    @Override
//    public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        return "Bearer " + getToken();
//    }
//
//    public static void setToken(String token) {
//        context().getStore(NAMESPACE).put("token", token);
//    }
//
//    public static String getToken() {
//        return context().getStore(NAMESPACE).get("token", String.class);
//    }
//
//    public static void setCode(String code) {
//        context().getStore(NAMESPACE).put("code", code);
//    }
//
//    public static String getCode() {
//        return context().getStore(NAMESPACE).get("code", String.class);
//    }
//
//    public static Cookie getJsessionIdCookie() {
//        return new Cookie(
//                "JSESSIONID",
//                ThreadSafeCookieStore.INSTANCE.cookieValue("JSESSIONID")
//        );
//    }
//}