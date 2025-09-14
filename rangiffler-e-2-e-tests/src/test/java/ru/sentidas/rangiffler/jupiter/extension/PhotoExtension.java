package ru.sentidas.rangiffler.jupiter.extension;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;
import ru.sentidas.rangiffler.jupiter.annotaion.User;
import ru.sentidas.rangiffler.model.Like;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.service.GeoDbClient;
import ru.sentidas.rangiffler.service.PhotoDbClient;
import ru.sentidas.rangiffler.utils.generator.PhotoDescriptions;
import ru.sentidas.rangiffler.utils.generator.RandomDataUtils;

import javax.annotation.Nullable;
import java.util.*;

import static ru.sentidas.rangiffler.utils.generator.UserDataGenerator.languageTagByCountry;

public class PhotoExtension implements BeforeEachCallback, ParameterResolver {

    public static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(PhotoExtension.class);
    private final PhotoDbClient photoClient = new PhotoDbClient();
    private final GeoDbClient geoClient = new GeoDbClient();


    @Override
    public void beforeEach(ExtensionContext context) {
        AnnotationSupport.findAnnotation(context.getRequiredTestMethod(), User.class)
                .ifPresent(anno -> {
                    ru.sentidas.rangiffler.model.User createdUser = UserExtension.createdUser();

                    final UUID userId = createdUser != null ? createdUser.id() : null;
                    final String userCountryCode = createdUser != null ? createdUser.countryCode() : null;

                    final List<Photo> createdPhotos = new ArrayList<>();
                    final List<String> countriesCode = geoClient.getCountriesCode();
                    final Random random = new Random();

                    final String fakerTag = languageTagByCountry(userCountryCode);

                    if (ArrayUtils.isNotEmpty(anno.photos())) {
                        for (ru.sentidas.rangiffler.jupiter.annotaion.Photo photoAnno : anno.photos()) {
                            final String defaultTravelCountyCode = countriesCode.get(random.nextInt(countriesCode.size()));
                            final String description = PhotoDescriptions.randomByTag(fakerTag);

                            Photo newPhoto = photoClient.createPhoto(new Photo(
                                    null,
                                    userId,
                                    !Objects.equals(photoAnno.src(), "") ? photoAnno.src() : RandomDataUtils.randomPhoto(),
                                    !Objects.equals(photoAnno.countryCode(), "") ? photoAnno.countryCode() : defaultTravelCountyCode,
                                    !Objects.equals(photoAnno.description(), "") ? photoAnno.description() : description,
                                    new Date(),
                                    0
                            ));

                            createdPhotos.add(newPhoto);

                            if (photoAnno.likes() > 0) {
                                int requested = photoAnno.likes();
                                List<ru.sentidas.rangiffler.model.User> friends = createdUser.testData().friends();
                                int n = Math.min(requested, friends.size());

                                for (int i = 0; i < n; i++) {
                                    UUID likerId = friends.get(i).id();
                                    photoClient.likePhoto(newPhoto.id(), likerId);
                                }
                            }
                        }
                    }

                    if (anno.photo() > 0) {
                        for (int i = 0; i < anno.photo(); i++) {
                            final String defaultTravelCountyCode = countriesCode.get(random.nextInt(countriesCode.size()));
                            final String description = PhotoDescriptions.randomByTag(fakerTag);

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
                    if (createdUser != null) {
                        createdUser.testData().photos().addAll(
                                createdPhotos
                        );
                    }
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
}
