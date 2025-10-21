package ru.sentidas.rangiffler.jupiter.extension;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.sentidas.rangiffler.jupiter.annotaion.Photo;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.AppPhoto;
import ru.sentidas.rangiffler.service.GeoDbClient;
import ru.sentidas.rangiffler.service.PhotoApiClient;
import ru.sentidas.rangiffler.utils.generation.GenerationDataUser;
import ru.sentidas.rangiffler.utils.generation.LocaleUtil;
import ru.sentidas.rangiffler.utils.generation.PhotoDescriptions;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static ru.sentidas.rangiffler.jupiter.annotaion.Photo.Owner;


public class PhotoExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PhotoExtension.class);

    private final PhotoApiClient photoClient = new PhotoApiClient();
    private final GeoDbClient geoClient = new GeoDbClient();


    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(anno -> {
                    AppUser createdUser = UserExtension.createdUser();

                    final UUID userId = createdUser != null ? createdUser.id() : null;
                    final String userCountryCode = createdUser != null ? createdUser.countryCode() : null;

                    // список друзей и «по сколько фото каждому»
                    final List<AppUser> friends = createdUser != null
                            ? createdUser.testData().friends()
                            : Collections.emptyList();

                    final int each = anno.friendsWithPhotosEach();

                    final List<AppPhoto> createdPhotos = new ArrayList<>();

                    final List<String> countriesCode = geoClient.getCountriesCode();
                    final String userLocaleTag = LocaleUtil.tagByCountry(userCountryCode);

                    // === 1) Фото из @User.photos()
                    if (ArrayUtils.isNotEmpty(anno.photos())) {
                        for (ru.sentidas.rangiffler.jupiter.annotaion.Photo photoAnno : anno.photos()) {

                            // [ADDED] — ветвление по владельцу фото
                            if (photoAnno.owner() == Owner.FRIEND) {
                                // [ADDED] — валидируем индекс друга
                                int index = photoAnno.partyIndex();
                                if (createdUser == null || createdUser.testData().friends().isEmpty()) {
                                    throw new IllegalStateException("[@Photo owner=FRIEND] no friends available for friendIndex=" + index);
                                }
                                if (index < 0 || index >= createdUser.testData().friends().size()) {
                                    throw new IllegalArgumentException("[@Photo owner=FRIEND] friendIndex out of bounds: " + index);
                                }

                                AppUser friend = createdUser.testData().friends().get(index);

                                final String friendLocaleTag = LocaleUtil.tagByCountry(friend.countryCode());

                                // [ADDED] — создаём count фото другу
                                for (int i = 0; i < photoAnno.count(); i++) {
//
                                    AppPhoto photo = createLocalizedPhoto(
                                            friend.id(),
                                            friendLocaleTag,                       // локаль описания — у друга
                                            nz(photoAnno.countryCode()),           // страна поездки (или случайная)
                                            nz(photoAnno.src()),                   // src (или из classpath через Gen)
                                            nzOrElse(photoAnno.description(),      // описание из аннотации или сгенерённое
                                                    "friend: " + PhotoDescriptions.pickRandom(friendLocaleTag)),
                                            photoClient,
                                            countriesCode
                                    );

                                    // лайк от основного юзера (только 1)
                                    int applied = 0;
                                    if (photoAnno.likes() > 0) {
                                        photoClient.likePhoto(photo.id(), createdUser.id());
                                        applied = 1;
                                        if (photoAnno.likes() != 1) {
                                            System.out.println("For a friend is possible only one like from the main user");
                                        }
                                    }

                                    AppPhoto createdWithLikes = photo.withLikesTotal(applied);
                                    friend.testData().photos().add(createdWithLikes);
                                    //   createdFriendsPhotos.add(createdWithLikes);

                                }
                            }
                            // [ADDED] — ветвление по владельцу фото INCOME
                            else if (photoAnno.owner() == Owner.INCOME) {
                                // [ADDED] — валидируем индекс друга
                                int index = photoAnno.partyIndex();
                                if (createdUser == null || createdUser.testData().incomeInvitations().isEmpty()) {
                                    throw new IllegalStateException("[@Photo owner=INCOME] no friends available for partyIndex=" + index);
                                }
                                if (index < 0 || index >= createdUser.testData().incomeInvitations().size()) {
                                    throw new IllegalArgumentException("[@Photo owner=INCOME] partyIndex out of bounds: " + index);
                                }

                                AppUser income = createdUser.testData().incomeInvitations().get(index);
                                final String incomeLocaleTag = LocaleUtil.tagByCountry(income.countryCode());


                                // [ADDED] — создаём count фото другу
                                for (int i = 0; i < photoAnno.count(); i++) {
                                    AppPhoto photo = createLocalizedPhoto(
                                            income.id(),
                                            incomeLocaleTag,
                                            nz(photoAnno.countryCode()),
                                            nz(photoAnno.src()),
                                            nzOrElse(photoAnno.description(),
                                                    "income: " + PhotoDescriptions.pickRandom(incomeLocaleTag)),
                                            photoClient,
                                            countriesCode
                                    );

                                    // [ADDED] — сохраняем в тестовые данные друга
                                   // createdIncomePhotos.add(photo);
                                    income.testData().photos().add(photo);
                                }

                                if (photoAnno.likes() > 0) {
                                    System.out.println("For an income user is not possible create likes");
                                }
                            } else if (photoAnno.owner() == Owner.OUTCOME) {

                                int index = photoAnno.partyIndex();
                                if (createdUser == null || createdUser.testData().outcomeInvitations().isEmpty()) {
                                    throw new IllegalStateException("[@Photo owner=OUTCOME] no friends available for partyIndex=" + index);
                                }
                                if (index < 0 || index >= createdUser.testData().outcomeInvitations().size()) {
                                    throw new IllegalArgumentException("[@Photo owner=OUTCOME] partyIndex out of bounds: " + index);
                                }

                                AppUser outcome = createdUser.testData().outcomeInvitations().get(index);
                                // [CHANGED] явная локаль по стране исходящего инвайта
                                final String outcomeLocaleTag = LocaleUtil.tagByCountry(outcome.countryCode());

                                for (int i = 0; i < photoAnno.count(); i++) {
                                    AppPhoto photo = createLocalizedPhoto(
                                            outcome.id(),
                                            outcomeLocaleTag,
                                            nz(photoAnno.countryCode()),
                                            nz(photoAnno.src()),
                                            nzOrElse(photoAnno.description(),
                                                    "outcome: " + PhotoDescriptions.pickRandom(outcomeLocaleTag)),
                                            photoClient,
                                            countriesCode
                                    );

                                    // [ADDED] — сохраняем в тестовые данные друга
                                   // createdOutcomePhotos.add(photo);
                                    outcome.testData().photos().add(photo);
                                }

                                if (photoAnno.likes() > 0) {
                                    System.out.println("For an outcome user is not possible create likes");
                                }
                            } else {
                                // SELF — фото текущего пользователя + поддержка count
                                for (int c = 0; c < photoAnno.count(); c++) {
                                    AppPhoto newPhoto = createLocalizedPhoto(
                                            userId,
                                            userLocaleTag,                       // локаль описания — у владельца
                                            nz(photoAnno.countryCode()),
                                            nz(photoAnno.src()),
                                            nz(photoAnno.description()),         // если пусто — сгенерим по локали
                                            photoClient,
                                            countriesCode
                                    );

                                    // лайки от друзей к фото пользователя
                                    int appliedLikes = 0;
                                    if (photoAnno.likes() > 0) {
                                        int requested = photoAnno.likes();
                                        int n = Math.min(requested, friends.size());
                                        for (int i = 0; i < n; i++) {
                                            UUID likerId = friends.get(i).id();
                                            photoClient.likePhoto(newPhoto.id(), likerId);
                                        }
                                        appliedLikes = n;
                                    }

                                    AppPhoto createdWithLikes = newPhoto.withLikesTotal(appliedLikes);
                                    createdPhotos.add(createdWithLikes);
                                }
                            }
                        }
                    }

                    // === 2) Фото пользователя по счётчику @User.photo()
                    if (anno.photo() > 0) {
                        for (int i = 0; i < anno.photo(); i++) {
                            AppPhoto ph = createLocalizedPhoto(
                                    userId,
                                    userLocaleTag,
                                    null, // страна поездки — случайная
                                    null,            // src — случайный из classpath
                                    null,            // описание — фраза по локали владельца
                                    photoClient,
                                    countriesCode
                            );
                            createdPhotos.add(ph);
                        }
                    }

                    // === 3) Каждому другу по each фото
                    if (each > 0 && !friends.isEmpty()) {
                        for (AppUser friend : friends) {
                            final String friendLocaleTag = LocaleUtil.tagByCountry(friend.countryCode());
                            for (int i = 0; i < each; i++) {
                                AppPhoto ph = createLocalizedPhoto(
                                        friend.id(),
                                        friendLocaleTag,
                                        null,
                                        null,
                                        "friend: " + PhotoDescriptions.pickRandom(friendLocaleTag),
                                        photoClient,
                                        countriesCode
                                );
                              //  createdFriendsPhotos.add(ph);
                                friend.testData().photos().add(ph);
                            }
                        }
                    }

                    // === 4) Записать фото тестового пользователя и обновить счётчик друзей
                    if (createdUser != null) {
                        createdUser.testData().photos().addAll(createdPhotos);
                    }

                    int friendsPhotosTotal = friends.stream()
                            .map(AppUser::testData)
                            .mapToInt(td -> td.photos().size())
                            .sum();

                    if (createdUser != null) {
                        createdUser = createdUser.withTestData(
                                createdUser.testData().withFriendsPhotosTotal(friendsPhotosTotal)
                        );
                        UserExtension.setUser(createdUser);
                    }
                   // === 5) Положить созданные фото в стор расширения
                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            createdPhotos);

                });
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().isAssignableFrom(Photo[].class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Photo[] resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (Photo[]) extensionContext.getStore(PhotoExtension.NAMESPACE)
                .get(extensionContext.getUniqueId(), List.class)
                .stream()
                .toArray(Photo[]::new);
    }

    private AppPhoto createLocalizedPhoto(
            UUID ownerId,
            String descriptionLocaleTag,
            String overrideTravelCountry,
            String overrideSrc,
            String overrideDescription,
            PhotoApiClient photoClient,
            List<String> countryPool
    ) {
        String travelCountry = (overrideTravelCountry != null && !overrideTravelCountry.isBlank())
                ? overrideTravelCountry
                : pickRandom(countryPool);

        String src = (overrideSrc != null && !overrideSrc.isBlank())
                ? overrideSrc
                : GenerationDataUser.randomPhotoDataUrl(); // [CHANGED] только classpath

        String locale = LocaleUtil.normalize(descriptionLocaleTag); // ru/en/es/zh-cn/pt-br...
        String description = (overrideDescription != null && !overrideDescription.isBlank())
                ? overrideDescription
                : PhotoDescriptions.pickRandom(locale);

        return photoClient.createPhoto(new AppPhoto(
                null,
                ownerId,
                src,
                travelCountry,
                description,
                new Date(),
                0
        ));
    }

    // утилиты мелочи
    private static String nz(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    private static String nzOrElse(String s, String fallback) {
        return (s == null || s.isBlank()) ? fallback : s;
    }

    private static <T> T pickRandom(List<T> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }
}
