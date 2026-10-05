package com.chudzik.weather_sensors.service;

import com.chudzik.weather_sensors.assembler.MeasurementOutputDtoAssembler;
import com.chudzik.weather_sensors.dto.MeasurementAddDto;
import com.chudzik.weather_sensors.dto.MeasurementOutputDto;
import com.chudzik.weather_sensors.dto.MeasurementQueryDto;
import com.chudzik.weather_sensors.exception.InvalidSensorApiKeyException;
import com.chudzik.weather_sensors.exception.MalformedQueryException;
import com.chudzik.weather_sensors.exception.ResourceNotFoundException;
import com.chudzik.weather_sensors.exception.UnprocessableException;
import com.chudzik.weather_sensors.model.Measurement;
import com.chudzik.weather_sensors.model.Sensor;
import com.chudzik.weather_sensors.model.Series;
import com.chudzik.weather_sensors.repository.MeasurementRepository;
import com.chudzik.weather_sensors.repository.SensorRepository;
import com.chudzik.weather_sensors.specification.MeasurementQuerySpecification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class MeasurementService {

    private final MeasurementRepository measurementRepo;
    private final SensorRepository sensorRepo;


    public MeasurementService(MeasurementRepository measurementRepo, SensorRepository sensorRepo) {
        this.measurementRepo = measurementRepo;
        this.sensorRepo = sensorRepo;
    }

    @Transactional
    public MeasurementOutputDto createMeasurement(String hashedSensorApiKey, MeasurementAddDto inputDto) {

        Sensor sensor = sensorRepo.findByApiKeyHash(hashedSensorApiKey)
                .orElseThrow(InvalidSensorApiKeyException::new);

        Series series = sensor.getSeries();

        Instant now = Instant.now();
        Instant timestamp = inputDto.timestamp();

        if (timestamp == null) {
            timestamp = now;
        }

        if (timestamp.isAfter(now.plusSeconds(300))) {
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

        return MeasurementOutputDtoAssembler.assemble(savedMeasurement);
    }

    private Pageable createPageable(MeasurementQueryDto query) {
        int limit = query.limit();

        Sort.Direction direction =
                "-timestamp".equals(query.sort())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;

        Sort sort = Sort.by(direction, "timestamp");

        return PageRequest.of(0, limit, sort);
    }

    public List<MeasurementOutputDto> getMeasurements(MeasurementQueryDto query) {
        if (query.limit() <= 0 || query.limit() > 10000) {
            throw new MalformedQueryException("Limit must be between 1 and 10000");
        }
        if (query.sort() != null
                && !query.sort().equals("timestamp")
                && !query.sort().equals("-timestamp")) {
            throw new MalformedQueryException("Invalid sort parameter");
        }
        Specification<Measurement> specification =
                MeasurementQuerySpecification.buildSpecification(query);

        Pageable pageable = createPageable(query);

        Page<Measurement> page = measurementRepo.findAll(specification, pageable);

        return page.getContent()
                .stream()
                .map(MeasurementOutputDtoAssembler::assemble)
                .toList();
    }

    public MeasurementOutputDto getMeasurement(Long id) {
        Measurement measurement = measurementRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("No measurement with id " + id + " found"));

        return MeasurementOutputDtoAssembler.assemble(measurement);
    }
}
