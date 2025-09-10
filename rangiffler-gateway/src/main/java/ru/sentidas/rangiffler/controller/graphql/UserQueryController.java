package ru.sentidas.rangiffler.controller.graphql;

import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import ru.sentidas.rangiffler.ex.TooManySubQueriesException;
import ru.sentidas.rangiffler.model.ggl.input.Country;
import ru.sentidas.rangiffler.model.ggl.input.UserGql;
import ru.sentidas.rangiffler.service.api.GeoClient;
import ru.sentidas.rangiffler.service.api.GrpcUserdataClient;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
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
public class UserQueryController {

    private final GrpcUserdataClient grpcUserdataClient;
    private final GeoClient geoClient;

    @Autowired
    public UserQueryController(GrpcUserdataClient grpcUserdataClient, GeoClient geoClient) {
        this.grpcUserdataClient = grpcUserdataClient;
        this.geoClient = geoClient;
    }

    @SchemaMapping(typeName = "User", field = "friends")
    public Slice<UserGql> friends(UserGql user,
                                  @Argument int page,
                                  @Argument int size,
                                  @Argument @Nullable String searchQuery) {
        return grpcUserdataClient.friends(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }

    @SchemaMapping(typeName = "User", field = "location")
    public Country location(UserGql user) {
        if (user.countryCode() == null || user.countryCode().isBlank()) return null;
        return geoClient.getByCode(user.countryCode());
    }

    @SchemaMapping(typeName = "User", field = "incomeInvitations")
    public Slice<UserGql> incomeInvitations(UserGql user,
                                            @Argument int page,
                                            @Argument int size,
                                            @Argument @Nullable String searchQuery) {
        return grpcUserdataClient.incomeInvitations(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }

    @SchemaMapping(typeName = "User", field = "outcomeInvitations")
    public Slice<UserGql> outcomeInvitations(UserGql user,
                                             @Argument int page,
                                             @Argument int size,
                                             @Argument @Nullable String searchQuery) {
        return grpcUserdataClient.outcomeInvitations(
                user.username(),
                PageRequest.of(page, size),
                searchQuery
        );
    }


    @QueryMapping
    public UserGql user(@AuthenticationPrincipal Jwt principal,
                        @Nonnull DataFetchingEnvironment env) {
        checkSubQueries(env, 2, "friends");
        final String username = principal.getClaim("sub");
        return grpcUserdataClient.currentUser(username);
    }

    @QueryMapping
    public Slice<UserGql> users(@AuthenticationPrincipal Jwt principal,
                                @Argument int page,
                                @Argument int size,
                                @Argument @Nullable String searchQuery,
                                @Nonnull DataFetchingEnvironment env) {
        final String username = principal.getClaim("sub");
        checkSubQueries(env, 2, "friends");
        checkSubQueries(env, 2, "incomeInvitations");
        checkSubQueries(env, 2, "outcomeInvitations");
        return grpcUserdataClient.allUsers(
                username,
                PageRequest.of(page, size),
                // new GqlQueryPaginationAndSort(page, size).pageable(), // подумать про добавление сортировки иили не усложнять, оставить Pageble
                searchQuery
        );
    }

    private void checkSubQueries(@Nonnull DataFetchingEnvironment env, int depth, @Nonnull String... queryKeys) {
        for (String queryKey : queryKeys) {
            List<SelectedField> selectors = env.getSelectionSet().getFieldsGroupedByResultKey().get(queryKey);
            if (selectors != null && selectors.size() > depth) {
                throw new TooManySubQueriesException("Can`t fetch over 2 " + queryKey + " sub-queries");
            }
        }
    }
}
