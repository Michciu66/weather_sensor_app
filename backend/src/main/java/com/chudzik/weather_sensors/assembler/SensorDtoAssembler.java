package com.chudzik.weather_sensors.assembler;

import com.chudzik.weather_sensors.dto.SensorDto;
import com.chudzik.weather_sensors.model.Sensor;

public class SensorDtoAssembler {

    static SensorDto assemble(Sensor sensor) {
        return new SensorDto(sensor.getId(), sensor.getName(), sensor.getSeries().getId(), sensor.getCreatedAt(),
                sensor.getLastMeasurementAt() );
    }
}
