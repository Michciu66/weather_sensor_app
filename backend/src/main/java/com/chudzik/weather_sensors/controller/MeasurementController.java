package com.chudzik.weather_sensors.controller;

import com.chudzik.weather_sensors.dto.MeasurementAddDto;
import com.chudzik.weather_sensors.dto.MeasurementOutputDto;
import com.chudzik.weather_sensors.dto.MeasurementQueryDto;
import com.chudzik.weather_sensors.service.ApiKeyService;
import com.chudzik.weather_sensors.service.MeasurementService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
public class MeasurementController {

    private final MeasurementService measurementService;
    private final ApiKeyService apiKeyService;

    public MeasurementController(MeasurementService measurementService, ApiKeyService apiKeyService) {
        this.measurementService = measurementService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping(
            path = "/api/measurements",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MeasurementOutputDto> createMeasurement(
            @RequestHeader("X-API-Key") String sensorApiKey,
            @RequestBody @Valid MeasurementAddDto inputDto
    ) {
        MeasurementOutputDto outputDto =
                measurementService.createMeasurement(
                        apiKeyService.hashApiKey(sensorApiKey),
                        inputDto
                );

        URI location = URI.create(
                "/api/measurements/" + outputDto.id()
        );

        return ResponseEntity
                .created(location)
                .body(outputDto);
    }

    @GetMapping( path = "/api/measurements",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<MeasurementOutputDto> > listMeasurements(@RequestBody @Valid MeasurementQueryDto inputDto )
    {
        List<MeasurementOutputDto> outputDtos = measurementService.getMeasurements( inputDto );

        return new ResponseEntity<>( outputDtos, HttpStatus.OK );
    }
}