package ru.sentidas.rangiffler.jupiter.extension;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.sentidas.rangiffler.model.TestData;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.service.UsersClient;
import ru.sentidas.rangiffler.service.UsersDbClient;
import ru.sentidas.rangiffler.utils.generator.RandomDataUtils;
import ru.sentidas.rangiffler.utils.generator.UserData;
import ru.sentidas.rangiffler.utils.generator.UserDataGenerator;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class UserExtension implements BeforeEachCallback, ParameterResolver {
    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(UserExtension.class);

    private static final String defaultPassword = "12345";
    private final UsersClient usersClient = new UsersDbClient();

    @Override
    public void beforeEach(ExtensionContext context) {

        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), ru.sentidas.rangiffler.jupiter.annotaion.User.class)
                .ifPresent(anno -> {
                    AppUser user;

                    if (anno.empty() == false) {

                        if ("".equals(anno.username())) {
                            // Случай 1: username не задан — создаём нового
                            final String username = RandomDataUtils.randomUsername();
                            final UserData userData = UserDataGenerator.randomUser();
                            final String firstName = userData.firstname();
                            final String surname = userData.surname();
                            final String countryCode = userData.countryCode();
                            final String avatar = RandomDataUtils.randomAvatar();

                            AppUser createdUser = new AppUser(
                                    null,
                                    username,
                                    firstName,
                                    surname,
                                    avatar,
                                    null,
                                    null,
                                    countryCode,
                                    new TestData(
                                            defaultPassword,
                                            new ArrayList<>(), // photos
                                            new ArrayList<>(), // friends
                                            new ArrayList<>(), // incomeInvitations
                                            new ArrayList<>()  // outcomeInvitations
                                    )
                            );

                            user = usersClient.createFullUser(createdUser);

                            List<AppUser> friends = new ArrayList<>();
                            List<AppUser> incomeInvitations = new ArrayList<>();
                            List<AppUser> outcomeInvitations = new ArrayList<>();

                            if (anno.friends() > 0) {
                                friends = usersClient.addFriends(user, anno.friends());
                            }
                            if (anno.incomeInvitation() > 0) {
                                incomeInvitations = usersClient.createIncomeInvitations(user, anno.incomeInvitation());
                            }
                            if (anno.outcomeInvitation() > 0) {
                                outcomeInvitations = usersClient.createOutcomeInvitations(user, anno.outcomeInvitation());
                            }

                            user.testData().friends().addAll(friends);
                            user.testData().incomeInvitations().addAll(incomeInvitations);
                            user.testData().outcomeInvitations().addAll(outcomeInvitations);

                        } else {
                            // Случай 2: username задан — ищем, если не найден в БД - пока ошибка
                            user = usersClient.findUserByUsername(anno.username())
                                    .orElseThrow(() -> new IllegalStateException("User " + anno.username() + " not found"))
                                    .withPassword(defaultPassword);
                        }

                        // cоздаем пустого user
                    } else {
                        if ("".equals(anno.username())) {
                            // Случай 1: username не задан — создаём нового
                            final String username = RandomDataUtils.randomUsername();
                            final String countryCode = "RU";

                            AppUser createdUser = new AppUser(
                                    null,
                                    username,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    countryCode,
                                    new TestData(
                                            defaultPassword,
                                            new ArrayList<>(), // photos
                                            new ArrayList<>(), // friends
                                            new ArrayList<>(), // incomeInvitations
                                            new ArrayList<>()  // outcomeInvitations
                                    )
                            );

                            user = usersClient.createFullUser(createdUser);

                            List<AppUser> friends = new ArrayList<>();
                            List<AppUser> incomeInvitations = new ArrayList<>();
                            List<AppUser> outcomeInvitations = new ArrayList<>();

                            if (anno.friends() > 0) {
                                friends = usersClient.addFriends(user, anno.friends());
                            }
                            if (anno.incomeInvitation() > 0) {
                                incomeInvitations = usersClient.createIncomeInvitations(user, anno.incomeInvitation());
                            }
                            if (anno.outcomeInvitation() > 0) {
                                outcomeInvitations = usersClient.createOutcomeInvitations(user, anno.outcomeInvitation());
                            }

                            user.testData().friends().addAll(friends);
                            user.testData().incomeInvitations().addAll(incomeInvitations);
                            user.testData().outcomeInvitations().addAll(outcomeInvitations);

                        } else {
                            // Случай 2: username задан — ищем, если не найден в БД - пока ошибка
                            user = usersClient.findUserByUsername(anno.username())
                                    .orElseThrow(() -> new IllegalStateException("User " + anno.username() + " not found"))
                                    .withPassword(defaultPassword);
                        }
                    }
                    setUser(user);
                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(AppUser.class);
    }

    @Override
    public AppUser resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
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
}