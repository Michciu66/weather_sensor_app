package com.chudzik.weather_sensors.exception;

public class MalformedQueryException extends RuntimeException {
    public MalformedQueryException(String message) {
        super(message);
    }
}
