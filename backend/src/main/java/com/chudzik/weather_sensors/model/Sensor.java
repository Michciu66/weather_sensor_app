package com.chudzik.weather_sensors.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table( name = "sensors", indexes = {
        @Index(name = "idx_sensors_apihashkey", columnList = "api_key_hash"),
})
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long id;

    @Column(name = "sensor_name", length = 100)
    private String name;

    @OneToMany(mappedBy = "sensor", fetch = FetchType.LAZY)
    private List<Measurement> measurementList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Column(name = "api_key_hash", nullable = false, unique = true)
    private String apiKeyHash;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "last_measurement_at")
    private Instant lastMeasurementAt;

    public Sensor( String name) { this();
        this.name = name;
    }

    public Sensor() { measurementList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<Measurement> getMeasurementList() {
        return measurementList;
    }

    public void setMeasurementList(List<Measurement> measurementList) {
        this.measurementList = measurementList;
    }

    public Series getSeries() {
        return series;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public void addMeasurement(Measurement measurement) {
        if (!measurementList.contains(measurement)) {
            this.measurementList.add(measurement);
        }
    }

    public void removeMeasurement(Measurement measurement) {
        measurementList.removeIf(n -> (n.equals(measurement)));
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastMeasurementAt() {
        return lastMeasurementAt;
    }

    public void setLastMeasurementAt(Instant lastMeasurementAt) {
        this.lastMeasurementAt = lastMeasurementAt;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Sensor sensor)) return false;
        return Objects.equals(id, sensor.id) && Objects.equals(name, sensor.name) && Objects.equals(apiKeyHash, sensor.apiKeyHash) && Objects.equals(createdAt, sensor.createdAt) && Objects.equals(lastMeasurementAt, sensor.lastMeasurementAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, apiKeyHash, createdAt, lastMeasurementAt);
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", createdAt=" + createdAt +
                ", lastMeasurementAt=" + lastMeasurementAt +
                '}';
    }
}
