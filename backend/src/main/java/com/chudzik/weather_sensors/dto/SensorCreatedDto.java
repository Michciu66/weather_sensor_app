package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;

public record SensorCreatedDto(@NotNull Long id,
                               @NotBlank String name,
                               @NotNull Long seriesId,
                               Instant createdAt,
                               Instant lastMeasurementAt,
                               @NotBlank @Length( min = 32 ) String apiKey) {
}
