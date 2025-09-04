package guru.qa.rangiffler.controller.graphql;

import guru.qa.rangiffler.model.User;
import guru.qa.rangiffler.service.UserService;
import guru.qa.rangiffler.service.api.GrpcUserdataClient;
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

import jakarta.annotation.Nullable;


@Controller
@PreAuthorize("isAuthenticated()")
public class UserQueryController {

    private final UserService userService;

    private final GrpcUserdataClient grpcUserdataClient;

    @Autowired
    public UserQueryController(UserService userService, GrpcUserdataClient grpcUserdataClient) {
        this.userService = userService;
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @SchemaMapping(typeName = "User", field = "friends")
    public Slice<User> friends(User user,
                               @Argument int page,
                               @Argument int size,
                               @Argument @Nullable String searchQuery) {
        return userService.friends(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }

    @SchemaMapping(typeName = "User", field = "incomeInvitations")
    public Slice<User> incomeInvitations(User user,
                                            @Argument int page,
                                            @Argument int size,
                                            @Argument @Nullable String searchQuery) {
        return userService.incomeInvitations(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }

    @SchemaMapping(typeName = "User", field = "outcomeInvitations")
    public Slice<User> outcomeInvitations(User user,
                                             @Argument int page,
                                             @Argument int size,
                                             @Argument @Nullable String searchQuery) {
        return userService.outcomeInvitations(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }


    @QueryMapping
    public User user(@AuthenticationPrincipal Jwt principal) {
        final String username = principal.getClaim("sub");
        return userService.getUser(username);
    }

    @QueryMapping
    public Slice<User> users(@AuthenticationPrincipal Jwt principal,
                             @Argument int page,
                             @Argument int size,
                             @Argument @Nullable String searchQuery) {
        final String username = principal.getClaim("sub");

        return userService.allUsers(
                username,
                PageRequest.of(page, size),
                searchQuery
        );
    }
}
