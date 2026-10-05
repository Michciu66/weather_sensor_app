package com.chudzik.weather_sensors.assembler;

import com.chudzik.weather_sensors.dto.MeasurementDto;
import com.chudzik.weather_sensors.model.Measurement;

public class MeasurementDtoAssembler {

    public static MeasurementDto assemble(Measurement measurement )
    {
        return new MeasurementDto(measurement.getId(),
                measurement.getSeries().getId(),
                measurement.getSensor() != null ? measurement.getId() : null,
                measurement.getValue(),
                measurement.getTimestamp() );
    }
}
