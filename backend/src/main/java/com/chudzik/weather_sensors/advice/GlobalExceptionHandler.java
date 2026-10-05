package com.chudzik.weather_sensors.advice;

import com.chudzik.weather_sensors.exception.*;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import org.springframework.web.bind.MissingRequestHeaderException;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFound(
            ResourceNotFoundException exception
    ) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Resource not found",
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidSensorApiKeyException.class)
    public ProblemDetail handleInvalidSensorApiKey(
            InvalidSensorApiKeyException exception
    ) {
        return createProblem(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                exception.getMessage()
        );
    }

    @ExceptionHandler(UnprocessableException.class)
    public ProblemDetail handleUnprocessable(
            UnprocessableException exception
    ) {
        return createProblem(
                HttpStatus.UNPROCESSABLE_ENTITY,
                "Unprocessable entity",
                exception.getMessage()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(
            ConflictException exception
    ) {
        return createProblem(
                HttpStatus.CONFLICT,
                "Conflict",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IncorrectLoginException.class)
    public ProblemDetail handleIncorrectUsernameException(
            IncorrectLoginException exception
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Incorrect Login or Password",
                exception.getMessage()
        );
    }

    @ExceptionHandler(MalformedQueryException.class)
    public ProblemDetail handleMalformedQueryException(
            MalformedQueryException exception
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Malformed Query",
                exception.getMessage()
        );
    }
    @ExceptionHandler(InvalidTokenException.class)
    public ProblemDetail handleInvalidTokenException(
            InvalidTokenException exception
    ) {
        return createProblem(
                HttpStatus.UNAUTHORIZED,
                "Invalid Token",
                exception.getMessage()
        );
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ProblemDetail handleIncorrectPassword(IncorrectPasswordException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, exception.getMessage());
        problem.setTitle("Forbidden");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request",
                "Request validation failed"
        );

        List<Map<String, String>> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", error.getDefaultMessage()
                ))
                .toList();

        problem.setProperty("errors", errors);

        return handleExceptionInternal(
                exception,
                problem,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                "Request body could not be read"
        );

        return handleExceptionInternal(
                exception,
                problem,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                "Unsupported media type",
                "Only application/json is supported"
        );

        return handleExceptionInternal(
                exception,
                problem,
                headers,
                HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                request
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        ProblemDetail problem = createProblem(
                HttpStatus.NOT_ACCEPTABLE,
                "Not acceptable",
                "The requested response format is not supported"
        );

        return handleExceptionInternal(
                exception,
                problem,
                headers,
                HttpStatus.NOT_ACCEPTABLE,
                request
        );
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<Object> handleMissingRequestHeader(
            MissingRequestHeaderException exception
    ) {
        if ("X-API-Key".equalsIgnoreCase(exception.getHeaderName())) {
            ProblemDetail problem = createProblem(
                    HttpStatus.UNAUTHORIZED,
                    "Unauthorized",
                    "Missing sensor API key"
            );

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(problem);
        }

        ProblemDetail problem = createProblem(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problem);
    }

    private ProblemDetail createProblem(
            HttpStatusCode status,
            String title,
            String detail
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                status,
                detail
        );

        problem.setTitle(title);

        return problem;
    }
}