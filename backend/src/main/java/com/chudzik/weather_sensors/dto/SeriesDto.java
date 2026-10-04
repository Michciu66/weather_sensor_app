package com.chudzik.weather_sensors.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SeriesDto {

    private Long id;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private String name;
    private String color;
    private String icon;
    private String unit;

    private List<MeasurementDto> measurementList;
    private List<SensorDto> sensors;

    public SeriesDto() {
        measurementList = new ArrayList<>();
        sensors = new ArrayList<>();
    }

    public SeriesDto(
            Long id,
            BigDecimal minValue,
            BigDecimal maxValue,
            String name,
            String color,
            String icon,
            String unit,
            List<MeasurementDto> measurementList,
            List<SensorDto> sensors
    ) {
        this.id = id;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.name = name;
        this.color = color;
        this.icon = icon;
        this.unit = unit;
        this.measurementList = measurementList;
        this.sensors = sensors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public List<MeasurementDto> getMeasurementList() {
        return measurementList;
    }

    public void setMeasurementList(List<MeasurementDto> measurementList) {
        this.measurementList = measurementList;
    }

    public List<SensorDto> getSensors() {
        return sensors;
    }

    public void setSensors(List<SensorDto> sensors) {
        this.sensors = sensors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SeriesDto that)) return false;

        return Objects.equals(id, that.id)
                && Objects.equals(minValue, that.minValue)
                && Objects.equals(maxValue, that.maxValue)
                && Objects.equals(name, that.name)
                && Objects.equals(color, that.color)
                && Objects.equals(icon, that.icon)
                && Objects.equals(unit, that.unit)
                && Objects.equals(measurementList, that.measurementList)
                && Objects.equals(sensors, that.sensors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                minValue,
                maxValue,
                name,
                color,
                icon,
                unit,
                measurementList,
                sensors
        );
    }

    @Override
    public String toString() {
        return "SeriesDto{" +
                "id=" + id +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
                ", unit='" + unit + '\'' +
                ", measurementList=" + measurementList +
                ", sensors=" + sensors +
                '}';
    }
}