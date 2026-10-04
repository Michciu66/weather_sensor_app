package com.chudzik.weather_sensors.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MeasurementInputDto(@NotNull BigDecimal value, OffsetDateTime timestamp) {

    @Override
    public String toString() {
        return "MeasurementInputDto[" +
                "value=" + value + ", " +
                "timestamp=" + timestamp + ']';
    }


}
