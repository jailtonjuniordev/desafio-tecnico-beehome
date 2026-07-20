package com.jjdev.beehome_tecnico.shared.infrastructure.config;

import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;
import com.jjdev.beehome_tecnico.shared.dto.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.List;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ErroResponseDTO errorResponse = new ErroResponseDTO("Method not supported: " + ex.getMethod());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_IMPLEMENTED);
    }

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex) {
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(err -> err.getField() + ": " + err.getDefaultMessage())
				.toList();
		ErroResponseDTO body = new ErroResponseDTO("Validation failed", errors);
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}
}
