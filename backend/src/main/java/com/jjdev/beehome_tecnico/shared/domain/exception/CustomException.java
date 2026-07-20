package com.jjdev.beehome_tecnico.shared.domain.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class CustomException extends RuntimeException{

    private final HttpStatus httpStatus;
    private final List<String> errors;

    public CustomException(String message, HttpStatus httpStatus) {
        this(message, null, httpStatus);
    }

    public CustomException(String message, List<String> errors, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
        this.errors = errors;
    }

}
