package com.chudzik.weather_sensors.assembler;

import com.chudzik.weather_sensors.dto.MeasurementOutputDto;
import com.chudzik.weather_sensors.model.Measurement;

public class MeasurementOutputDtoAssembler {

    public static MeasurementOutputDto assemble(Measurement measurement )
    {
        return new MeasurementOutputDto(
                measurement.getId(),
                measurement.getSensor().getId(),
                measurement.getSeries().getId(),
                measurement.getValue(),
                measurement.getTimestamp() );
    }
}
