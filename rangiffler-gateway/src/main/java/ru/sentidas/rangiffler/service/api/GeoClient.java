package ru.sentidas.rangiffler.service.api;

import ru.sentidas.rangiffler.model.Stat;
import ru.sentidas.rangiffler.model.ggl.input.Country;

import java.util.List;

public interface GeoClient {

    Country getByCode(String code);

    List<Country> getByCodes(List<String> codes);

    List<Country> countries();

    List<Stat> stat(String username, boolean withFriends);
}
