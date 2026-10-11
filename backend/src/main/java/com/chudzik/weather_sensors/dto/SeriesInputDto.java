package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record SeriesInputDto(@NotBlank @Length(min = 1, max = 100) String name, @NotNull BigDecimal minValue,
                             @NotNull BigDecimal maxValue, @NotNull @Pattern(regexp = "^#[0-9A-Fa-f]{6}$") String color,
                             String icon, @Size( max = 20 )String unit) {
}
