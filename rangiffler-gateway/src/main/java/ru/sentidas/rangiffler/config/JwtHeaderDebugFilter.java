package ru.sentidas.rangiffler.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class JwtHeaderDebugFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtHeaderDebugFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        if ("/graphql".equals(req.getRequestURI())) {
            String auth = req.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    String[] parts = auth.substring(7).split("\\.");
                    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
                    log.info("JWT header on {} {} -> {}", req.getMethod(), req.getRequestURI(), headerJson); // ждём {"alg":"RS256","kid":"..."}
                } catch (Exception e) {
                    log.warn("JWT header parse failed", e);
                }
            } else {
                log.info("No Bearer token on {} {}", req.getMethod(), req.getRequestURI());
            }
        }
        chain.doFilter(req, res);
    }
}
