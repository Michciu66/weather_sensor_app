package com.chudzik.weather_sensors.controller;

import com.chudzik.weather_sensors.dto.SensorCreatedDto;
import com.chudzik.weather_sensors.dto.SensorDto;
import com.chudzik.weather_sensors.dto.SensorInputDto;
import com.chudzik.weather_sensors.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Controller
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping(path = "/api/sensors", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SensorDto>> getAllSensors() {
        List<SensorDto> sensors = sensorService.getAllSensors();
        return ResponseEntity.ok(sensors);
    }

    @PostMapping(path = "/api/sensors",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SensorCreatedDto> createSensor(@RequestBody @Valid SensorInputDto inputDto) {
        SensorCreatedDto sensor = sensorService.createSensor(inputDto);
        URI location = URI.create("/api/sensors/" + sensor.id());

        return ResponseEntity.created(location).body(sensor);
    }

    @GetMapping(path = "/api/sensors/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<SensorDto> getSensor(@PathVariable Long id) {
        return ResponseEntity.ok(sensorService.getSensor(id));
    }

    @DeleteMapping(path = "/api/sensors/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSeries(@PathVariable Long id) {
        sensorService.removeSensor(id);
    }

}
