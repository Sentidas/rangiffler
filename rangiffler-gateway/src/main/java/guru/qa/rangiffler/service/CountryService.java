package guru.qa.rangiffler.service;

import guru.qa.rangiffler.model.Country;
import guru.qa.rangiffler.repository.CountryRepository;
import guru.qa.rangiffler.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CountryService {


    private final CountryRepository countryRepository;
    private static final Logger LOG = LoggerFactory.getLogger(CountryService.class);

    @Autowired
    public CountryService(UserRepository userRepository, CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List<Country> countries() {
        return countryRepository.findAll()
                .stream()
                .map(ce ->
                        new Country(
                                ce.getCode(),
                                ce.getName(),
                                ce.getFlag()
                        )).toList();
    }
}
