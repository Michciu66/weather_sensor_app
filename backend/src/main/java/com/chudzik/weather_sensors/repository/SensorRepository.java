package com.chudzik.weather_sensors.repository;

import com.chudzik.weather_sensors.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findByApiKeyHash(String apiKeyHash);
}