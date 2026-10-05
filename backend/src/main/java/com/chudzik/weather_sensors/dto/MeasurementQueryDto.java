package com.chudzik.weather_sensors.dto;

import java.time.OffsetDateTime;

public record MeasurementQueryDto(String series,
                                  OffsetDateTime from,
                                  OffsetDateTime to,
                                  String sort,
                                  Integer limit ) {
}
