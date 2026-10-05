package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public record MeasurementAddDto(@NotNull BigDecimal value, Instant timestamp) {

    @Override
    public String toString() {
        return "MeasurementInputDto[" +
                "value=" + value + ", " +
                "timestamp=" + timestamp + ']';
    }


}
