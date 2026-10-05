package com.chudzik.weather_sensors.assembler;

import com.chudzik.weather_sensors.dto.SeriesDto;
import com.chudzik.weather_sensors.model.Series;

public class SeriesDtoAssembler {
    public static SeriesDto assemble(Series series) {
        return new SeriesDto(
                series.getId(), series.getMinValue(), series.getMaxValue(), series.getName(), series.getColor(), series.getIcon(),
                series.getUnit(), series.getMeasurementList().stream().map(MeasurementDtoAssembler::assemble).toList(),
                series.getSensors().stream().map(SensorDtoAssembler::assemble).toList());
    }
}
