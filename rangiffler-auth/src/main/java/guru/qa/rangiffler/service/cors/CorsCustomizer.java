package guru.qa.rangiffler.service.cors;

import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CorsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Component
public class CorsCustomizer {

  private final String rangifflerFrontUri;
  private final String rangifflerAuthUri;

  @Autowired
  public CorsCustomizer(@Value("${rangiffler-front.base-uri}") String rangifflerFrontUri,
                        @Value("${rangiffler-auth.base-uri}") String rangifflerAuthUri) {
    this.rangifflerFrontUri = rangifflerFrontUri;
    this.rangifflerAuthUri = rangifflerAuthUri;
  }

  public void corsCustomizer(@Nonnull HttpSecurity http) throws Exception {
    http.cors(customizer());
  }

  Customizer<CorsConfigurer<HttpSecurity>> customizer() {
    return c -> c.configurationSource(corsConfigurationSource());
  }

  CorsConfigurationSource corsConfigurationSource() {
    return request -> {
      CorsConfiguration cc = new CorsConfiguration();
      cc.setAllowCredentials(true);
      cc.setAllowedOrigins(List.of(rangifflerFrontUri, rangifflerAuthUri));
      cc.setAllowedHeaders(List.of("*"));
      cc.setAllowedMethods(List.of("*"));
      return cc;
    };
  }
}
