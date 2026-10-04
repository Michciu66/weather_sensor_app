package com.chudzik.weather_sensors.repository;

import com.chudzik.weather_sensors.model.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Long> {
}