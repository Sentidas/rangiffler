package ru.sentidas.rangiffler.controller.graphql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.model.Feed;

import ru.sentidas.rangiffler.model.Likes;
import ru.sentidas.rangiffler.model.Photo;
import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.model.ggl.input.Country;
import ru.sentidas.rangiffler.model.ggl.input.UserGql;
import ru.sentidas.rangiffler.service.api.GrpcGeoClient;
import ru.sentidas.rangiffler.service.api.GrpcPhotoClient;

import java.util.List;
@Slf4j
@Controller
@PreAuthorize("isAuthenticated()")
public class FeedQueryController {

    private final GrpcPhotoClient grpcPhotoClient;
    private final GrpcGeoClient grpcGeoClient;

    public FeedQueryController(GrpcPhotoClient grpcPhotoClient, GrpcGeoClient grpcGeoClient) {
        this.grpcPhotoClient = grpcPhotoClient;
        this.grpcGeoClient = grpcGeoClient;
    }


    @SchemaMapping(typeName = "Feed", field = "stat")
    public List<Stat> stat(Feed feed) {
        return grpcGeoClient.stat(
                feed.username(),
                feed.withFriends()
        );
    }

    @SchemaMapping(typeName = "Photo", field = "likes")
    public Likes likes(Photo photo) {
        return photo.likes() != null ? photo.likes() : new Likes(0, List.of());
    }

    @SchemaMapping(typeName = "User", field = "photos")
    public Slice<Photo> photos(UserGql user,
                               @Argument int page,
                               @Argument int size) {
        return grpcPhotoClient.userPhotos(
                user.username(),
                PageRequest.of(page, size)
        );
    }

//    @SchemaMapping(typeName = "Photo", field = "country")
//    public Country country(Photo photo) {
//        String code = photo.countryCode();
//
//        if (code == null || code.isBlank()) {
//            log.warn("Photo {} has empty countryCode, returning Unknown", photo.id());
//            return unknownCountry();
//        }
//
//        Country c = grpcGeoClient.getByCode(code);
//        if (c == null) {
//            log.warn("Country not found for code={}, photo={}, returning Unknown", code, photo.id());
//            return unknownCountry();
//        }
//        log.info("Resolving Photo.country for photo {}", photo.id());
//        return c;
//    }

    private Country unknownCountry() {
        // Подгони под свой конструктор/record Country(flag, code, name)
        return new Country("XX", "Unknown", "🏳️");
    }


    @SchemaMapping(typeName = "Feed", field = "photos")
    public Slice<Photo> photos(Feed feed,
                               @Argument int page,
                               @Argument int size) {
        return feed.withFriends()
                ? grpcPhotoClient.feedPhotos(
                feed.username(),
                PageRequest.of(page, size)
        )
                : grpcPhotoClient.userPhotos(
                feed.username(),
                PageRequest.of(page, size)
        );
    }

    @QueryMapping
    public Feed feed(@AuthenticationPrincipal Jwt principal,
                     @Argument boolean withFriends) {
        return new Feed(
                principal.getClaim("sub"),
                withFriends,
                null,
                null
        );
    }
}
