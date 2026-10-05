package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record SensorInputDto(@NotBlank @Length(min = 1, max = 100) String name, @NotNull Long seriesId) {
}
