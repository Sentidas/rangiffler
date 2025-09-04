package guru.qa.rangiffler.controller.graphql;

import guru.qa.rangiffler.model.*;
import guru.qa.rangiffler.service.PhotoService;
import guru.qa.rangiffler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class FeedQueryController {

    private final UserService userService;
    private final PhotoService photoService;

    @Autowired
    public FeedQueryController(UserService userService, PhotoService photoService) {
        this.userService = userService;
        this.photoService = photoService;
    }

    @SchemaMapping(typeName = "Feed", field = "stat")
    public List<Stat> stat(Feed feed) {
        return userService.stat(
                feed.username(),
                feed.withFriends()
        );
    }

    @SchemaMapping(typeName = "Photo", field = "likes")
    public Likes likes(Photo photo) {
        List<Like> likes = photoService.photoLikes(
                photo.id()
        );
        return new Likes(
                likes.size(),
                likes
        );
    }

    @SchemaMapping(typeName = "User", field = "photos")
    public Slice<Photo> photos(User user,
                               @Argument int page,
                               @Argument int size) {
        return photoService.allUserPhotos(
                user.username(),
                PageRequest.of(page, size)
        );
    }

    @SchemaMapping(typeName = "Feed", field = "photos")
    public Slice<Photo> photos(Feed feed,
                               @Argument int page,
                               @Argument int size) {
        return feed.withFriends()
                ? photoService.feedPhotos(
                feed.username(),
                PageRequest.of(page, size)
        )
                : photoService.allUserPhotos(
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
