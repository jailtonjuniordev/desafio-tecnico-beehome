package com.jjdev.beehome_tecnico.shared.infrastructure.web;

import com.jjdev.beehome_tecnico.shared.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ex) {
        ErroResponseDTO errorResponse = new ErroResponseDTO(ex.getMessage(), ex.getErrors());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        ErroResponseDTO errorResponse = new ErroResponseDTO("Email or password incorrect");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}
