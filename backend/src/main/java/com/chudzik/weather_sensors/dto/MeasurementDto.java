package com.chudzik.weather_sensors.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;

public class MeasurementDto {

    private Long id;
    private Long seriesId;
    private Long sensorId;
    private BigDecimal value;
    private Instant timestamp;

    public MeasurementDto() {
    }

    public MeasurementDto(
            Long id,
            Long seriesId,
            Long sensorId,
            BigDecimal value,
            Instant timestamp
    ) {
        this.id = id;
        this.seriesId = seriesId;
        this.sensorId = sensorId;
        this.value = value;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MeasurementDto that)) return false;

        return Objects.equals(value, that.value)
                && Objects.equals(id, that.id)
                && Objects.equals(seriesId, that.seriesId)
                && Objects.equals(sensorId, that.sensorId)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                seriesId,
                sensorId,
                value,
                timestamp
        );
    }

    @Override
    public String toString() {
        return "MeasurementDto{" +
                "id=" + id +
                ", seriesId=" + seriesId +
                ", sensorId=" + sensorId +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }
}