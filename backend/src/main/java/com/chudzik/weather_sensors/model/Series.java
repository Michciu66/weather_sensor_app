package com.chudzik.weather_sensors.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "series")
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "series_id")
    Long id;

    @Column(name = "series_min", nullable = false)
    BigDecimal minValue;

    @Column(name = "series_max", nullable = false)
    BigDecimal maxValue;

    @Column(name = "series_name", nullable = false, length = 50)
    private String name;

    @Column(name = "series_color", nullable = false, length = 7)
    private String color;

    @Column( name = "series_icon")
    private String icon;

    @Column( name = "series_unit", length = 20)
    private String unit;

    @OneToMany(mappedBy = "series", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Measurement> measurementList = new ArrayList<>();

    @OneToMany(mappedBy = "series", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private List<Sensor> sensors = new ArrayList<>();

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public List<Measurement> getMeasurementList() {
        return measurementList;
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

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public void setMeasurementList(List<Measurement> measurementList) {
        this.measurementList = measurementList;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Series series)) return false;
        return Objects.equals(id, series.id) && Objects.equals(minValue, series.minValue) && Objects.equals(maxValue, series.maxValue) && Objects.equals(name, series.name) && Objects.equals(color, series.color) && Objects.equals(icon, series.icon) && Objects.equals(unit, series.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, minValue, maxValue, name, color, icon, unit);
    }

    @Override
    public String toString() {
        return "Series{" +
                "Id=" + id +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", name='" + name + '\'' +
                ", color='" + color + '\'' +
                ", icon='" + icon + '\'' +
                ", unit='" + unit + '\'' +
                '}';
    }
}
