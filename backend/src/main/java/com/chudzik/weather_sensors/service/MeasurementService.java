package com.chudzik.weather_sensors.service;

import com.chudzik.weather_sensors.dto.MeasurementInputDto;
import com.chudzik.weather_sensors.dto.MeasurementOutputDto;
import com.chudzik.weather_sensors.exception.InvalidSensorApiKeyException;
import com.chudzik.weather_sensors.exception.UnprocessableException;
import com.chudzik.weather_sensors.model.Measurement;
import com.chudzik.weather_sensors.model.Sensor;
import com.chudzik.weather_sensors.model.Series;
import com.chudzik.weather_sensors.repository.MeasurementRepository;
import com.chudzik.weather_sensors.repository.SensorRepository;
import com.chudzik.weather_sensors.repository.SeriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepo;
    private final SensorRepository sensorRepo;


    public MeasurementService(MeasurementRepository measurementRepo, SensorRepository sensorRepo) {
        this.measurementRepo = measurementRepo;
        this.sensorRepo = sensorRepo;
    }

    @Transactional
    public MeasurementOutputDto createMeasurement(String hashedSensorApiKey, MeasurementInputDto inputDto )
    {

        Sensor sensor = sensorRepo.findByApiKeyHash(hashedSensorApiKey)
                .orElseThrow(InvalidSensorApiKeyException::new);

        Series series = sensor.getSeries();

        OffsetDateTime now = OffsetDateTime.now();

        OffsetDateTime timestamp = inputDto.timestamp();

        if (timestamp == null) {
            timestamp = now;
        }

        if (timestamp.isAfter(now.plusMinutes(5))) {
            throw new UnprocessableException(
                    "Measurement timestamp cannot be more than 5 minutes in the future"
            );
        }

        BigDecimal value = inputDto.value();

        if (value.compareTo(series.getMinValue()) < 0
                || value.compareTo(series.getMaxValue()) > 0) {
            throw new UnprocessableException(
                    "Measurement value is outside the allowed range"
            );
        }

        Measurement measurement = new Measurement(
                series,
                sensor,
                value,
                timestamp
        );

        Measurement savedMeasurement = measurementRepo.save(measurement);

        sensor.setLastMeasurementAt(timestamp);

        return new MeasurementOutputDto(
                savedMeasurement.getId(),
                series.getId(),
                sensor.getId(),
                savedMeasurement.getValue(),
                savedMeasurement.getTimestamp()
        );

    }
}
