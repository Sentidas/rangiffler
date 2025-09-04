package guru.qa.rangiffler.controller.graphql;

import guru.qa.rangiffler.model.Country;
import guru.qa.rangiffler.service.CountryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;

import java.util.List;


@Controller
@PreAuthorize("isAuthenticated()")
public class CountryQueryController {

    private final CountryService countryService;


    @Autowired
    public CountryQueryController(CountryService countryService) {
        this.countryService = countryService;
    }


    @QueryMapping
    public List<Country> countries(@AuthenticationPrincipal Jwt principal) {
        return countryService.countries();
    }
}
