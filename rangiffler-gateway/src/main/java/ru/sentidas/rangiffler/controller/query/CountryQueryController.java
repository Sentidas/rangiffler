package ru.sentidas.rangiffler.controller.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import ru.sentidas.rangiffler.model.Country;
import ru.sentidas.rangiffler.service.api.GrpcGeoClient;

import java.util.List;

@Controller
@PreAuthorize("isAuthenticated()")
public class CountryQueryController {

    private final GrpcGeoClient geoClient;

    @Autowired
    public CountryQueryController(GrpcGeoClient geoClient) {
        this.geoClient = geoClient;
    }

    @QueryMapping
    public List<Country> countries(@AuthenticationPrincipal Jwt principal) {
        return geoClient.countries();
    }
}
