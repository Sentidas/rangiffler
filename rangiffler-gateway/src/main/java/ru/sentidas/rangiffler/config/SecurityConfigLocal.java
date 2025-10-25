package ru.sentidas.rangiffler.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import ru.sentidas.rangiffler.error.http.Json401AuthenticationEntryPoint;
import ru.sentidas.rangiffler.service.cors.CorsCustomizer;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@Profile({"local", "docker"})
public class SecurityConfigLocal {

    private final CorsCustomizer corsCustomizer;

    public SecurityConfigLocal(CorsCustomizer corsCustomizer) {
        this.corsCustomizer = corsCustomizer;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   ObjectMapper objectMapper) throws Exception {

        AuthenticationEntryPoint json401 = new Json401AuthenticationEntryPoint(objectMapper);

        corsCustomizer.corsCustomizer(http);

        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/session/current",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/media/**",
                                "/graphiql/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/graphql").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(json401))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .authenticationEntryPoint(json401)
                        .jwt(Customizer.withDefaults())
                );

        return http.build();
    }
}
