package com.chudzik.weather_sensors.service;

import com.chudzik.weather_sensors.assembler.SeriesResponseDtoAssembler;
import com.chudzik.weather_sensors.dto.SeriesInputDto;
import com.chudzik.weather_sensors.dto.SeriesResponseDto;
import com.chudzik.weather_sensors.exception.ConflictException;
import com.chudzik.weather_sensors.exception.ResourceNotFoundException;
import com.chudzik.weather_sensors.exception.UnprocessableException;
import com.chudzik.weather_sensors.model.Measurement;
import com.chudzik.weather_sensors.model.Series;
import com.chudzik.weather_sensors.repository.SeriesRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeriesService {
    private final SeriesRepository seriesRepo;

    public SeriesService(SeriesRepository seriesRepo) {
        this.seriesRepo = seriesRepo;
    }

    public List<SeriesResponseDto> getAllSeries() {
        return seriesRepo.findAll().stream().map(SeriesResponseDtoAssembler::assemble).toList();
    }

    @Transactional
    public SeriesResponseDto create(SeriesInputDto input) {
        if (input.minValue().compareTo(input.maxValue()) >= 0) {
            throw new UnprocessableException("minValue must be less than maxValue");
        }

        Series series = new Series();
        series.setMinValue(input.minValue());
        series.setMaxValue(input.maxValue());
        series.setName(input.name());
        series.setColor(input.color());
        series.setIcon(input.icon());
        series.setUnit(input.unit());

        Series savedSeries = seriesRepo.save(series);

        return SeriesResponseDtoAssembler.assemble(savedSeries);
    }

    public SeriesResponseDto getSeries(Long id) {
        Series series = seriesRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " doesn't exist"));

        return SeriesResponseDtoAssembler.assemble(series);
    }

    @Transactional
    public SeriesResponseDto modifySeries(Long id, SeriesInputDto input) {
        if (input.minValue().compareTo(input.maxValue()) >= 0) {
            throw new UnprocessableException("minValue must be less than maxValue");
        }

        Series series = seriesRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " doesn't exist"));

        for (Measurement measurement : series.getMeasurementList()) {
            if (measurement.getValue().compareTo(input.minValue()) < 0 || measurement.getValue().compareTo(input.maxValue()) > 0) {
                throw new ConflictException("Proposed minmax values would invalidate a measurement");
            }
        }
        series.setMinValue(input.minValue());
        series.setMaxValue(input.maxValue());
        series.setName(input.name());
        series.setColor(input.color());
        series.setIcon(input.icon());
        series.setUnit(input.unit());

        Series savedSeries = seriesRepo.save(series);

        return SeriesResponseDtoAssembler.assemble(savedSeries);
    }

    @Transactional
    public void removeSeries(Long id) {
        Series series = seriesRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Series with id " + id + " doesn't exist"));
        seriesRepo.delete(series);
    }
}
