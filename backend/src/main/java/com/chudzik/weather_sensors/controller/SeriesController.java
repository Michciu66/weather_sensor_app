package com.chudzik.weather_sensors.controller;

import com.chudzik.weather_sensors.dto.SeriesInputDto;
import com.chudzik.weather_sensors.dto.SeriesResponseDto;
import com.chudzik.weather_sensors.service.SeriesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Controller
public class SeriesController {
    private final SeriesService seriesService;

    public SeriesController(SeriesService seriesService) {
        this.seriesService = seriesService;
    }


    @GetMapping(path = "/api/series", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<SeriesResponseDto>> getAllSeries() {
        return ResponseEntity.ok(seriesService.getAllSeries());
    }

    @PostMapping(path = "/api/series", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponseDto> createSeries(@Valid @RequestBody SeriesInputDto input) {
        SeriesResponseDto createdSeries = seriesService.create(input);
        URI location = URI.create("/api/series/" + createdSeries.id());

        return ResponseEntity.created(location).body(createdSeries);
    }

    @GetMapping(path = "/api/series/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponseDto> getSeries(@PathVariable Long id) {
        SeriesResponseDto series = seriesService.getSeries(id);
        return new ResponseEntity<>(series, HttpStatus.OK);
    }

    @PutMapping(path = "/api/series/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SeriesResponseDto> modifySeries(@PathVariable Long id, @RequestBody @Valid SeriesInputDto input) {
        SeriesResponseDto series = seriesService.modifySeries(id, input);
        return new ResponseEntity<>(series, HttpStatus.OK);
    }

    @DeleteMapping(path = "/api/series/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeSeries(@PathVariable Long id) {
        seriesService.removeSeries(id);
    }

}
