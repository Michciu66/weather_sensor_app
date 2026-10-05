package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginInputDto(@NotBlank String username, @NotBlank String password) {
}
