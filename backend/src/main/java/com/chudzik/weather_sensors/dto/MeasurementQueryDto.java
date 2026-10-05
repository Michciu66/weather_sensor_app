package com.chudzik.weather_sensors.dto;

import java.time.Instant;

public record MeasurementQueryDto(String series,
                                  Instant from,
                                  Instant to,
                                  String sort,
                                  Integer limit) {
}
