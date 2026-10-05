package com.chudzik.weather_sensors.controller;

import com.chudzik.weather_sensors.dto.LoginInputDto;
import com.chudzik.weather_sensors.dto.LoginResponseDto;
import com.chudzik.weather_sensors.dto.PasswordChangeDto;
import com.chudzik.weather_sensors.exception.InvalidTokenException;
import com.chudzik.weather_sensors.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping(path = "/api/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<LoginResponseDto> logIn(@RequestBody @Valid LoginInputDto input) {
        LoginResponseDto output = authService.logIn(input);
        return new ResponseEntity<>(output, HttpStatus.OK);

    }

    @PostMapping(path = "/api/auth/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logOut(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        if (!authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        String token = authorization.substring(7);
        authService.logOut(token);
    }

    @PutMapping(path = "/api/auth/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization, @RequestBody @Valid PasswordChangeDto input) {
        if (!authorization.startsWith("Bearer ")) {
            throw new InvalidTokenException();
        }
        String token = authorization.substring(7);
        authService.changePassword(token, input);
    }

}
