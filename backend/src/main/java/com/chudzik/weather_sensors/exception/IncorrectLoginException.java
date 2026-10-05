package com.chudzik.weather_sensors.exception;

public class IncorrectLoginException extends RuntimeException {

    public IncorrectLoginException() {
        super("Incorrect Login Information");
    }
}