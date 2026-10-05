package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record MeasurementOutputDto(Long id, Long seriesId, Long sensorId, BigDecimal value, Instant timestamp) {

    @Override
    public @NotNull String toString() {
        return "MeasurementOutputDto[" +
                "id=" + id + ", " +
                "seriesId=" + seriesId + ", " +
                "sensorId=" + sensorId + ", " +
                "value=" + value + ", " +
                "timestamp=" + timestamp + ']';
    }

}
