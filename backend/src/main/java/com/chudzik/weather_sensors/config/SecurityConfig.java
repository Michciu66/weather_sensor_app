package com.chudzik.weather_sensors.config;

import com.chudzik.weather_sensors.security.AuthTokenFilter;
import com.chudzik.weather_sensors.security.RestAuthenticationEntryPoint;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthTokenFilter authTokenFilter,
            RestAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable())
                .logout(logout -> logout.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/api/health", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/series", "/api/series/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/measurements", "/api/measurements/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/measurements").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/logout").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/auth/password").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/series").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/series/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/series/**").authenticated()
                        .requestMatchers("/api/sensors", "/api/sensors/**").authenticated()
                        .anyRequest().denyAll()
                )
                .addFilterBefore(authTokenFilter, AnonymousAuthenticationFilter.class);

        return http.build();
    }
}