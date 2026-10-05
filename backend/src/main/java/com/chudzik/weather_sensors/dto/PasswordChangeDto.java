package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordChangeDto(@NotBlank String currentPassword, @NotBlank @Size(min=8) String newPassword ) {
}
