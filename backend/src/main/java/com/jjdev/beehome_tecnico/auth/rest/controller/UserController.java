package com.jjdev.beehome_tecnico.auth.rest.controller;

import org.springframework.http.HttpStatus;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody UserCreateRequestDTO request) {
		return new ResponseEntity<>(userService.create(request), HttpStatus.CREATED);
	}

	@GetMapping("/me")
	public ResponseEntity<UserResponseDTO> getLogged() {
		return new ResponseEntity<>(userService.getLogged(), HttpStatus.OK);
	}

	@PutMapping("/me")
	public ResponseEntity<UserResponseDTO> update(@Valid @RequestBody UserUpdateRequestDTO request) {
		return new ResponseEntity<>(userService.update(request), HttpStatus.OK);
	}
}
