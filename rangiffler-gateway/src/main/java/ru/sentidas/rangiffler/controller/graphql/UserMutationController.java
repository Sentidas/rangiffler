package ru.sentidas.rangiffler.controller.graphql;

import ru.sentidas.rangiffler.model.ggl.input.UserGql;
import ru.sentidas.rangiffler.model.ggl.input.FriendshipInput;
import ru.sentidas.rangiffler.model.ggl.input.UserGqlInput;
import ru.sentidas.rangiffler.service.api.GrpcUserdataClient;
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

    private final GrpcUserdataClient grpcUserdataClient;

    @Autowired
    public UserMutationController(GrpcUserdataClient grpcUserdataClient) {
        this.grpcUserdataClient = grpcUserdataClient;
    }

    @MutationMapping
    public UserGql user(@AuthenticationPrincipal Jwt principal,
                        @Argument UserGqlInput input) {
        String username = principal.getClaim("sub");
        return grpcUserdataClient.updateUser(username, input);
    }

    @MutationMapping
    public UserGql friendship(@AuthenticationPrincipal Jwt principal,
                              @Argument FriendshipInput input) {

        String username = principal.getClaim("sub");
        return switch (input.action()) {
            case ADD -> grpcUserdataClient.sendInvitation(username, input);
            case ACCEPT -> grpcUserdataClient.acceptInvitation(username, input);
            case REJECT -> grpcUserdataClient.rejectInvitation(username, input);
            case DELETE -> grpcUserdataClient.deleteFriend(username, input);
        };
    }
}

