package com.chudzik.weather_sensors.dto;

public record LoginResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}