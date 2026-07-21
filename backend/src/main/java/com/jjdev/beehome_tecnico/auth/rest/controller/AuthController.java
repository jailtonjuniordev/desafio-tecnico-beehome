package com.jjdev.beehome_tecnico.auth.rest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jjdev.beehome_tecnico.auth.application.service.AuthService;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
		return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
	}
}
