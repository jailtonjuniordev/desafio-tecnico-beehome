package com.jjdev.beehome_tecnico.auth.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjdev.beehome_tecnico.auth.application.service.AuthService;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginResponseDTO;
import com.jjdev.beehome_tecnico.shared.dto.ErroResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user by email and password and returns a JWT access token. Invalid credentials return a generic 400 without revealing which field failed.")
    @ApiResponse(responseCode = "200", description = "Authenticated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = LoginResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Invalid credentials or validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }
}
