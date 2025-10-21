package ru.sentidas.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.service.UsersClient;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.UserData;

import javax.annotation.Nullable;

public class UserExtension implements BeforeEachCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);

    private static final String defaultPassword = "12345";
    private final UsersClient usersDbClient = new UsersDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.println("[INFO] Userdata storage mode: " + usersDbClient.getStorageMode());

        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(anno -> {
                    AppUser user;

                    // 1) Если username задан — берем из БД
                    if (!anno.username().isBlank()) {
                        user = usersDbClient.findUserByUsername(anno.username()).withPassword(defaultPassword);
                    } else {
                        // 2) Иначе создаём по флагу full
                        if (anno.full()) {
                            user = usersDbClient.createFullUser(randomFullUser());
                        } else {
                            user = usersDbClient.createUser(GenerationDataUser.randomUsername(), defaultPassword);
                        }
                    }

                    // 3) Друзья (минимальные/полные)
                    if (anno.friends() > 0) {
                        usersDbClient.addFriends(user, anno.friends())
                                .forEach(f -> user.testData().friends().add(f));
                    }
                    if (anno.fullFriends() > 0) {
                        usersDbClient.addFullFriends(user, anno.fullFriends())
                                .forEach(f -> user.testData().friends().add(f));
                    }

                    // 4) Входящие инвайты (друг -> user): минимальные + полные
                    if (anno.incomeInvitation() > 0) {
                        usersDbClient.createIncomeInvitations(user, anno.incomeInvitation(), false)
                                .forEach(inv -> user.testData().incomeInvitations().add(inv));
                    }
                    if (anno.fullIncomeInvitation() > 0) {
                        usersDbClient.createIncomeInvitations(user, anno.fullIncomeInvitation(), true)
                                .forEach(inv -> user.testData().incomeInvitations().add(inv));
                    }

                    // 5) Исходящие инвайты (user -> друг): минимальные + полные
                    if (anno.outcomeInvitation() > 0) {
                        usersDbClient.createOutcomeInvitations(user, anno.outcomeInvitation(), false)
                                .forEach(out -> user.testData().outcomeInvitations().add(out));
                    }
                    if (anno.fullOutcomeInvitation() > 0) {
                        usersDbClient.createOutcomeInvitations(user, anno.fullOutcomeInvitation(), true)
                                .forEach(out -> user.testData().outcomeInvitations().add(out));
                    }

                    setUser(user);
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(AppUser.class);
    }

    @Override
    public AppUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws
            ParameterResolutionException {
        return createdUser();
    }

    static void setUser(AppUser user) {
        ExtensionContext context = TestMethodContextExtension.context();
        context.getStore(NAMESPACE).put(
                context.getUniqueId(),
                user
        );
    }

    public static @Nullable AppUser createdUser() {
        final ExtensionContext context = TestMethodContextExtension.context();
        return context.getStore(NAMESPACE).get(context.getUniqueId(), AppUser.class);
    }

    private AppUser randomFullUser() {
        final String username = GenerationDataUser.randomUsername();
        final UserData userdata = GenerationDataUser.randomUser();
        final String avatar = GenerationDataUser.randomAvatarDataUrl();
        return new AppUser(
                null,
                username,
                userdata.firstname(),
                userdata.surname(),
                avatar,
                null,
                null,
                userdata.countryCode(),
                new TestData(defaultPassword)
        );
    }
}