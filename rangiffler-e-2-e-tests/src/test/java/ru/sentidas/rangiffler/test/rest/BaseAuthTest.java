package ru.sentidas.rangiffler.test.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import ru.sentidas.rangiffler.config.Config;
import ru.sentidas.rangiffler.jupiter.annotaion.meta.RestTest;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;
import ru.sentidas.rangiffler.rest.core.ThreadSafeCookieStore;
import ru.sentidas.rangiffler.service.AuthApiClient;

@RestTest
public abstract class BaseAuthTest {

    protected static final Config CFG = Config.getInstance();

    @RegisterExtension
    protected static final ApiLoginExtension apiLoginExtension = ApiLoginExtension.rest();

    protected final AuthApiClient authClientFollowRedirect = new AuthApiClient(true);
    protected final AuthApiClient authClientNoRedirect   = new AuthApiClient(false);

    @AfterEach
    void cleanCookies() {
        ThreadSafeCookieStore.INSTANCE.removeAll();

    }
}


