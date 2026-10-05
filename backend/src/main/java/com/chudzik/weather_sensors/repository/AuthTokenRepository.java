package com.chudzik.weather_sensors.repository;

import com.chudzik.weather_sensors.model.Admin;
import com.chudzik.weather_sensors.model.AuthToken;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long>  {
    @EntityGraph(attributePaths = "admin")
    Optional<AuthToken> findByTokenHash(String tokenHash );
}
