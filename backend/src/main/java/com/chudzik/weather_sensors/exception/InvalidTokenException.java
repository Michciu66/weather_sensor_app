package com.chudzik.weather_sensors.exception;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException() {
        super("Invalid security token");
    }
}
