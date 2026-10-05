package com.chudzik.weather_sensors.security;

import com.chudzik.weather_sensors.model.Admin;
import com.chudzik.weather_sensors.model.AuthToken;
import com.chudzik.weather_sensors.repository.AuthTokenRepository;
import com.chudzik.weather_sensors.service.HashService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private final AuthTokenRepository authTokenRepo;

    public AuthTokenFilter(AuthTokenRepository authTokenRepo) {
        this.authTokenRepo = authTokenRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String rawToken = authorization.substring(7);
        String tokenHash = HashService.sha256(rawToken);

        AuthToken authToken = authTokenRepo.findByTokenHash(tokenHash).orElse(null);

        if (authToken != null && authToken.getRevokedAt() == null && authToken.getExpiresAt().isAfter(Instant.now())) {

            Admin admin = authToken.getAdmin();

            var authentication = new UsernamePasswordAuthenticationToken(admin.getUsername(), null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}