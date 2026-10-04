package com.chudzik.weather_sensors.exception;

public class InvalidSensorApiKeyException extends RuntimeException {

    public InvalidSensorApiKeyException() {
        super("Invalid sensor API key");
    }
}