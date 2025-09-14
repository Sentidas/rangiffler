package ru.sentidas.rangiffler.api.core;

import okhttp3.Interceptor;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import ru.sentidas.rangiffler.jupiter.extension.ApiLoginExtension;

import java.io.IOException;
import java.util.Objects;

public class CodeInterceptor implements Interceptor {
  @Override
  public Response intercept(Chain chain) throws IOException {
    final Response response = chain.proceed(chain.request());
    if (response.isRedirect()) {
      String location = Objects.requireNonNull(
          response.header("Location")
      );
      if (location.contains("code=")) {
        ApiLoginExtension.setCode(
            StringUtils.substringAfter(
                location, "code="
            )
        );
      }
    }
    return response;
  }
}
