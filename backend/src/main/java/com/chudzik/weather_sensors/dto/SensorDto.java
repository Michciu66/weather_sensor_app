package com.chudzik.weather_sensors.dto;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SensorDto {

    private Long id;
    private String name;
    private Long seriesId;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastMeasurementAt;

    private List<MeasurementDto> measurementList;

    public SensorDto() {
        measurementList = new ArrayList<>();
    }

    public SensorDto(
            Long id,
            String name,
            Long seriesId,
            OffsetDateTime createdAt,
            OffsetDateTime lastMeasurementAt,
            List<MeasurementDto> measurementList
    ) {
        this.id = id;
        this.name = name;
        this.seriesId = seriesId;
        this.createdAt = createdAt;
        this.lastMeasurementAt = lastMeasurementAt;
        this.measurementList = measurementList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSeriesId() {
        return seriesId;
    }

    public void setSeriesId(Long seriesId) {
        this.seriesId = seriesId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getLastMeasurementAt() {
        return lastMeasurementAt;
    }

    public void setLastMeasurementAt(OffsetDateTime lastMeasurementAt) {
        this.lastMeasurementAt = lastMeasurementAt;
    }

    public List<MeasurementDto> getMeasurementList() {
        return measurementList;
    }

    public void setMeasurementList(List<MeasurementDto> measurementList) {
        this.measurementList = measurementList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SensorDto that)) return false;

        return Objects.equals(id, that.id)
                && Objects.equals(name, that.name)
                && Objects.equals(seriesId, that.seriesId)
                && Objects.equals(createdAt, that.createdAt)
                && Objects.equals(lastMeasurementAt, that.lastMeasurementAt)
                && Objects.equals(measurementList, that.measurementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                name,
                seriesId,
                createdAt,
                lastMeasurementAt,
                measurementList
        );
    }

    @Override
    public String toString() {
        return "SensorDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", seriesId=" + seriesId +
                ", createdAt=" + createdAt +
                ", lastMeasurementAt=" + lastMeasurementAt +
                ", measurementList=" + measurementList +
                '}';
    }
}