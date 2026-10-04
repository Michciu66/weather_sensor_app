package com.chudzik.weather_sensors.repository;

import com.chudzik.weather_sensors.model.Measurement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
}