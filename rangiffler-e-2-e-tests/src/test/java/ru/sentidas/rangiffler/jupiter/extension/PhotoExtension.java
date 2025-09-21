package ru.sentidas.rangiffler.jupiter.extension;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.AppUser;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.service.GeoDbClient;
import ru.sentidas.rangiffler.service.PhotoApiClient;
import ru.sentidas.rangiffler.service.PhotoDbClient;
import ru.sentidas.rangiffler.utils.generator.PhotoDescriptions;
import ru.sentidas.rangiffler.utils.generator.RandomDataUtils;

import java.util.*;

import static ru.sentidas.rangiffler.utils.generator.UserDataGenerator.languageTagByCountry;

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
                    List<AppUser> friends = createdUser !=null ?  createdUser.testData().friends() : Collections.emptyList();
                    final int each = anno.friendsWithPhotosEach();

                    final List<Photo> createdPhotos = new ArrayList<>();
                    final List<Photo> createdFriendsPhotos = new ArrayList<>();
                    final List<String> countriesCode = geoClient.getCountriesCode();
                    final Random random = new Random();

                    final String fakerTagUser = languageTagByCountry(userCountryCode);

                    // === 1) Фото пользователя из @User.photos()
                    if (ArrayUtils.isNotEmpty(anno.photos())) {
                        for (ru.sentidas.rangiffler.jupiter.annotaion.Photo photoAnno : anno.photos()) {
                            final String defaultTravelCountyCode = countriesCode.get(random.nextInt(countriesCode.size()));
                            final String description = PhotoDescriptions.randomByTag(fakerTagUser);

                            Photo newPhoto = photoClient.createPhoto(new Photo(
                                    null,
                                    userId,
                                    !Objects.equals(photoAnno.src(), "") ? photoAnno.src() : RandomDataUtils.randomPhoto(),
                                    !Objects.equals(photoAnno.countryCode(), "") ? photoAnno.countryCode() : defaultTravelCountyCode,
                                    !Objects.equals(photoAnno.description(), "") ? photoAnno.description() : description,
                                    new Date(),
                                    0
                            ));

                            // createdPhotos.add(newPhoto);

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
                            // Кладём в тестовые данные уже с верным likesTotal
                            Photo createdWithLikes = newPhoto.withLikesTotal(appliedLikes);
                            createdPhotos.add(createdWithLikes);
                        }
                    }

                    // === 2) Фото пользователя по счётчику @User.photo()
                    if (anno.photo() > 0) {
                        for (int i = 0; i < anno.photo(); i++) {
                            final String defaultTravelCountyCode = countriesCode.get(random.nextInt(countriesCode.size()));
                            final String description = PhotoDescriptions.randomByTag(fakerTagUser);

                            createdPhotos.add(
                                    photoClient.createPhoto(new Photo(
                                            null,
                                            userId,
                                            RandomDataUtils.randomPhoto(),
                                            defaultTravelCountyCode,
                                            description,
                                            new Date(),
                                            0
                                    ))
                            );
                        }
                    }
                    if(each > 0 && !friends.isEmpty()) {
                        for (AppUser friend : friends) {
                            final String friendLangTag = languageTagByCountry(friend.countryCode());

                            for (int i = 0; i < each; i++) {
                                final String defaultTravelCountyCodeFriend = countriesCode.get(random.nextInt(countriesCode.size()));
                                final String descriptionFriend = "friend: " + PhotoDescriptions.randomByTag(friendLangTag);

                                Photo photo = photoClient.createPhoto(new Photo(
                                        null,
                                        friend.id(),
                                        RandomDataUtils.randomPhoto(),
                                        defaultTravelCountyCodeFriend,
                                        descriptionFriend,
                                        new Date(),
                                        0
                                ));

                                createdFriendsPhotos.add(photo);
                                friend.testData().photos().add(photo);
                            }
                        }
                    }

                    if (createdUser != null) {
                        createdUser.testData().photos().addAll(
                                createdPhotos
                        );
                    }
                    context.getStore(NAMESPACE).put(
                            context.getUniqueId(),
                            createdPhotos);

                    context.getStore(NAMESPACE).put(
                            context.getUniqueId() + ":friends_photos",
                            createdFriendsPhotos);
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
}
