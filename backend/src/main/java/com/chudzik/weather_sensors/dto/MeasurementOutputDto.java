package com.chudzik.weather_sensors.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Objects;

public record MeasurementOutputDto(Long id, Long seriesId, Long sensorId, BigDecimal value, OffsetDateTime timestamp) {

    @Override
    public String toString() {
        return "MeasurementOutputDto[" +
                "id=" + id + ", " +
                "seriesId=" + seriesId + ", " +
                "sensorId=" + sensorId + ", " +
                "value=" + value + ", " +
                "timestamp=" + timestamp + ']';
    }

}
