package guru.qa.rangiffler.controller.graphql;

import guru.qa.rangiffler.model.User;
import guru.qa.rangiffler.model.input.FriendshipInput;
import guru.qa.rangiffler.model.input.UserInput;
import guru.qa.rangiffler.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;


@Controller
@PreAuthorize("isAuthenticated()")
public class UserMutationController {

    private final UserService userService;


    @Autowired
    public UserMutationController(UserService userService) {
        this.userService = userService;
    }


    @MutationMapping
    public User user(@AuthenticationPrincipal Jwt principal,
                     @Argument UserInput input) {
        String username = principal.getClaim("sub");
        return userService.updateUser(username, input);
    }

    @MutationMapping
    public User friendship(@AuthenticationPrincipal Jwt principal,
                           @Argument FriendshipInput input) {

        String username = principal.getClaim("sub");
        return switch (input.action()) {
            case ADD -> userService.addFriend(username, input.user());
            case ACCEPT -> userService.acceptInvitation(username, input.user());
            case REJECT -> userService.rejectInvitation(username, input.user());
            case DELETE -> userService.deleteFriend(username, input.user());
        };
    }
}

