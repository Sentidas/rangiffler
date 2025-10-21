package ru.sentidas.rangiffler.rest.core;

import lombok.Getter;
import okhttp3.Interceptor;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;

import java.io.IOException;
import java.util.Objects;

@Getter
public class CodeInterceptor implements Interceptor {

    private String lastCode;

    @NotNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        final Response response = chain.proceed(chain.request());
        if (response.isRedirect()) {
            String location = Objects.requireNonNull(
                    response.header("Location")
            );
            if (location.contains("code=")) {
                final String code = StringUtils.substringAfter(location, "code=");

                this.lastCode = code;
                ApiLoginExtension.setCode(code);
            }
        }
        return response;
    }

    public void clearLastCode() {
        this.lastCode = null;
    }
}
