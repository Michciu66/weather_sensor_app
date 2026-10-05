package com.chudzik.weather_sensors.dto;

import java.time.Instant;

public record SensorDto(Long id, String name, Long seriesId, Instant createdAt, Instant lastMeasurementAt) {
}