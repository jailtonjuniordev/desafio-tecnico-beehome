package com.jjdev.beehome_tecnico.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jjdev.beehome_tecnico.auth.application.service.UserService;
import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginResponseDTO;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

	@Mock
	private UserService userService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtServiceUtils jwtServiceUtils;

	@InjectMocks
	private AuthServiceImpl authService;

	@Test
	void login_success() {
		UUID userId = UUID.randomUUID();
		UserModel user = UserModel.builder()
				.id(userId)
				.email("user@email.com")
				.password("hashed")
				.build();
		LoginRequestDTO request = LoginRequestDTO.builder()
				.email("user@email.com")
				.password("plain")
				.build();

		when(userService.getUserByEmailSystem("user@email.com")).thenReturn(user);
		when(passwordEncoder.matches("plain", "hashed")).thenReturn(true);
		when(jwtServiceUtils.generate(userId, "user@email.com")).thenReturn("jwt-token");
		when(jwtServiceUtils.getExpirationMs()).thenReturn(3600000L);

		LoginResponseDTO response = authService.login(request);

		assertEquals("jwt-token", response.token());
		assertEquals("Bearer", response.tokenType());
		assertEquals(3600000L, response.expiration());
		verify(jwtServiceUtils).generate(userId, "user@email.com");
	}

	@Test
	void login_userNotFound_throwsBadCredentials() {
		LoginRequestDTO request = LoginRequestDTO.builder()
				.email("missing@email.com")
				.password("plain")
				.build();

		when(userService.getUserByEmailSystem("missing@email.com")).thenThrow(new CustomException("User not found", HttpStatus.NOT_FOUND));

		assertThrows(BadCredentialsException.class, () -> authService.login(request));
	}

	@Test
	void login_wrongPassword_throwsBadCredentials() {
		UserModel user = UserModel.builder()
				.id(UUID.randomUUID())
				.email("user@email.com")
				.password("hashed")
				.build();
		LoginRequestDTO request = LoginRequestDTO.builder()
				.email("user@email.com")
				.password("wrong")
				.build();

		when(userService.getUserByEmailSystem("user@email.com")).thenReturn(user);
		when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

		assertThrows(BadCredentialsException.class, () -> authService.login(request));
	}

	@Test
	void login_otherCustomException_rethrows() {
		LoginRequestDTO request = LoginRequestDTO.builder()
				.email("user@email.com")
				.password("plain")
				.build();
		CustomException conflict = new CustomException("Conflict", HttpStatus.CONFLICT);

		when(userService.getUserByEmailSystem("user@email.com")).thenThrow(conflict);

		CustomException thrown = assertThrows(CustomException.class, () -> authService.login(request));
		assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
		assertEquals("Conflict", thrown.getMessage());
	}
}
