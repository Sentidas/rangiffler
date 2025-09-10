package ru.sentidas.rangiffler.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import ru.sentidas.rangiffler.service.cors.CorsCustomizer;


@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@Profile("local")
public class SecurityConfigLocal {

    private final CorsCustomizer corsCustomizer;

    @Autowired
    public SecurityConfigLocal(CorsCustomizer corsCustomizer) {
        this.corsCustomizer = corsCustomizer;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtHeaderDebugFilter dbg) throws Exception { // <-- вот он, dbg
        corsCustomizer.corsCustomizer(http);

        http.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(dbg, BearerTokenAuthenticationFilter.class) // подключили лог-фильтр
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/session/current",
                                "/actuator/health",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/graphiql/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.POST, "/graphql").permitAll() // если хотите оставить /graphql публичным
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
}

