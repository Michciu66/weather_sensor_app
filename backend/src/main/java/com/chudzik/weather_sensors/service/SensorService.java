package com.chudzik.weather_sensors.service;

import com.chudzik.weather_sensors.assembler.SensorDtoAssembler;
import com.chudzik.weather_sensors.dto.SensorCreatedDto;
import com.chudzik.weather_sensors.dto.SensorDto;
import com.chudzik.weather_sensors.dto.SensorInputDto;
import com.chudzik.weather_sensors.exception.ResourceNotFoundException;
import com.chudzik.weather_sensors.exception.UnprocessableException;
import com.chudzik.weather_sensors.model.Sensor;
import com.chudzik.weather_sensors.model.Series;
import com.chudzik.weather_sensors.repository.SensorRepository;
import com.chudzik.weather_sensors.repository.SeriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class SensorService {

    private final SensorRepository sensorRepo;
    private final SeriesRepository seriesRepo;
    private final ApiKeyService apiKeyService;

    public SensorService(SensorRepository sensorRepo, SeriesRepository seriesRepo, ApiKeyService apiKeyService) {
        this.sensorRepo = sensorRepo;
        this.seriesRepo = seriesRepo;
        this.apiKeyService = apiKeyService;
    }

    public List<SensorDto> getAllSensors() {
        return sensorRepo.findAll().stream().map(SensorDtoAssembler::assemble).toList();
    }

    @Transactional
    public SensorCreatedDto createSensor(SensorInputDto input) {
        Series series = seriesRepo.findById(input.seriesId()).orElseThrow(() -> new UnprocessableException("No series with id " + input.seriesId() + " found"));

        Sensor sensor = new Sensor();
        sensor.setName(input.name());
        sensor.setSeries(series);
        sensor.setCreatedAt(Instant.now());

        String apiKey = apiKeyService.generateApiKey();
        sensor.setApiKeyHash(apiKeyService.hashApiKey(apiKey));

        Sensor savedSensor = sensorRepo.save(sensor);

        return new SensorCreatedDto(savedSensor.getId(),
                savedSensor.getName(),
                savedSensor.getSeries().getId(),
                savedSensor.getCreatedAt(),
                savedSensor.getLastMeasurementAt(),
                apiKey);
    }

    public SensorDto getSensor(Long id) {
        Sensor sensor = sensorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("No sensor with id " + id + " found"));

        return SensorDtoAssembler.assemble(sensor);
    }

    @Transactional
    public void removeSensor(Long id) {
        Sensor sensor = sensorRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sensor with id " + id + " doesn't exist"));

        sensor.getMeasurementList().forEach(measurement -> measurement.setSensor(null));
        sensorRepo.delete(sensor);
    }
}
