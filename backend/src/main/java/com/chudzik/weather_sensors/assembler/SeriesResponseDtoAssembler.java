package com.chudzik.weather_sensors.assembler;

import com.chudzik.weather_sensors.dto.SeriesResponseDto;
import com.chudzik.weather_sensors.model.Series;

public class SeriesResponseDtoAssembler {
    public static SeriesResponseDto assemble(Series series) {
        return new SeriesResponseDto(
                series.getId(), series.getMinValue(), series.getMaxValue(), series.getName(), series.getColor(), series.getIcon(),
                series.getUnit());
    }
}
