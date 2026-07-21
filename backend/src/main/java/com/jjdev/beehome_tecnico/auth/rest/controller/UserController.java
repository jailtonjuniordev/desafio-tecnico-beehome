package com.jjdev.beehome_tecnico.auth.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjdev.beehome_tecnico.auth.application.service.UserService;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;
import com.jjdev.beehome_tecnico.shared.dto.ErroResponseDTO;
import com.jjdev.beehome_tecnico.shared.infrastructure.config.OpenApiConfig;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User registration and profile endpoints")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register user", description = "Creates a new user account. Email must be unique. Password is stored with BCrypt.")
    @ApiResponse(responseCode = "201", description = "User created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateRequestDTO request) {
        return new ResponseEntity<>(userService.create(request), HttpStatus.CREATED);
    }

    @GetMapping("/me")
    @Operation(summary = "Get logged user", description = "Returns the profile of the authenticated user based on the JWT subject.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @ApiResponse(responseCode = "200", description = "Current user",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> getLogged() {
        return new ResponseEntity<>(userService.getLogged(), HttpStatus.OK);
    }

    @PutMapping("/me")
    @Operation(summary = "Update logged user", description = "Updates the authenticated user profile. If email changes, it must remain unique. Password is optional; when provided it is re-hashed with BCrypt.")
    @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
    @ApiResponse(responseCode = "200", description = "User updated",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = UserResponseDTO.class)))
    @ApiResponse(responseCode = "400", description = "Validation failed",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "401", description = "Authentication required",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    @ApiResponse(responseCode = "409", description = "Email already registered",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ErroResponseDTO.class)))
    public ResponseEntity<UserResponseDTO> update(@Valid @RequestBody UserUpdateRequestDTO request) {
        return new ResponseEntity<>(userService.update(request), HttpStatus.OK);
    }
}
