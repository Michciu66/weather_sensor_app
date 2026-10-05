package com.chudzik.weather_sensors.dto;

import java.math.BigDecimal;

public record SeriesResponseDto(Long id,
                          BigDecimal minValue,
                          BigDecimal maxValue,
                          String name,
                          String color,
                          String icon,
                          String unit
) {
}
