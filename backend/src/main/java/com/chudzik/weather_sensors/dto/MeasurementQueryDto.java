package com.chudzik.weather_sensors.dto;

import java.time.Instant;
import java.time.OffsetDateTime;

public record MeasurementQueryDto(String series,
                                  Instant from,
                                  Instant to,
                                  String sort,
                                  Integer limit ) {
}
