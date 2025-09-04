package guru.qa.rangiffler.service.utils;

import jakarta.annotation.Nullable;

import java.nio.charset.StandardCharsets;

public class StringAsBytes {
  private final String value;

  public StringAsBytes(@Nullable String value) {
    this.value = value;
  }

  public @Nullable byte[] bytes() {
    return value != null
        ? value.getBytes(StandardCharsets.UTF_8)
        : null;
  }
}
