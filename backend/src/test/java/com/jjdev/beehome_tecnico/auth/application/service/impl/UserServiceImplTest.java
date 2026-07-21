package com.jjdev.beehome_tecnico.auth.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.entity.UserEntity;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.repository.UserEntityRepository;
import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserEntityRepository userEntityRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtServiceUtils jwtServiceUtils;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	void create_success() {
		UserCreateRequestDTO request = UserCreateRequestDTO.builder()
				.username("jailton")
				.email("jailton@email.com")
				.password("senha1234")
				.build();
		UUID userId = UUID.randomUUID();
		UserEntity saved = UserEntity.builder()
				.id(userId)
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();

		when(userEntityRepository.existsByEmail("jailton@email.com")).thenReturn(false);
		when(passwordEncoder.encode("senha1234")).thenReturn("hashed");
		when(userEntityRepository.save(any(UserEntity.class))).thenReturn(saved);

		UserResponseDTO response = userService.create(request);

		assertEquals(userId, response.id());
		assertEquals("jailton", response.username());
		assertEquals("jailton@email.com", response.email());
		verify(userEntityRepository).save(any(UserEntity.class));
	}

	@Test
	void create_emailConflict_throws409() {
		UserCreateRequestDTO request = UserCreateRequestDTO.builder()
				.username("jailton")
				.email("jailton@email.com")
				.password("senha1234")
				.build();

		when(userEntityRepository.existsByEmail("jailton@email.com")).thenReturn(true);

		CustomException thrown = assertThrows(CustomException.class, () -> userService.create(request));
		assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
		verify(userEntityRepository, never()).save(any());
	}

	@Test
	void getLogged_success() {
		UUID userId = UUID.randomUUID();
		UserEntity entity = UserEntity.builder()
				.id(userId)
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(userEntityRepository.findById(userId)).thenReturn(Optional.of(entity));

		UserResponseDTO response = userService.getLogged();

		assertEquals(userId, response.id());
		assertEquals("jailton", response.username());
	}

	@Test
	void getLogged_notFound_throws404() {
		UUID userId = UUID.randomUUID();
		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(userEntityRepository.findById(userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> userService.getLogged());
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}

	@Test
	void update_success() {
		UUID userId = UUID.randomUUID();
		UserEntity entity = UserEntity.builder()
				.id(userId)
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();
		UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
				.username("jailton2")
				.email("jailton2@email.com")
				.password("senha5678")
				.build();
		UserEntity saved = UserEntity.builder()
				.id(userId)
				.username("jailton2")
				.email("jailton2@email.com")
				.password("hashed2")
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(userEntityRepository.findById(userId)).thenReturn(Optional.of(entity));
		when(userEntityRepository.existsByEmail("jailton2@email.com")).thenReturn(false);
		when(passwordEncoder.encode("senha5678")).thenReturn("hashed2");
		when(userEntityRepository.save(any(UserEntity.class))).thenReturn(saved);

		UserResponseDTO response = userService.update(request);

		assertEquals("jailton2", response.username());
		assertEquals("jailton2@email.com", response.email());
	}

	@Test
	void update_emailConflict_throws409() {
		UUID userId = UUID.randomUUID();
		UserEntity entity = UserEntity.builder()
				.id(userId)
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();
		UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
				.username("jailton")
				.email("other@email.com")
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(userEntityRepository.findById(userId)).thenReturn(Optional.of(entity));
		when(userEntityRepository.existsByEmail("other@email.com")).thenReturn(true);

		CustomException thrown = assertThrows(CustomException.class, () -> userService.update(request));
		assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
		verify(userEntityRepository, never()).save(any());
	}

	@Test
	void update_userNotFound_throws404() {
		UUID userId = UUID.randomUUID();
		UserUpdateRequestDTO request = UserUpdateRequestDTO.builder()
				.username("jailton")
				.email("jailton@email.com")
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(userEntityRepository.findById(userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> userService.update(request));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}

	@Test
	void getUserByIdSystem_success() {
		UUID userId = UUID.randomUUID();
		UserEntity entity = UserEntity.builder()
				.id(userId)
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();

		when(userEntityRepository.findById(userId)).thenReturn(Optional.of(entity));

		UserModel model = userService.getUserByIdSystem(userId);

		assertEquals(userId, model.getId());
		assertEquals("jailton@email.com", model.getEmail());
	}

	@Test
	void getUserByIdSystem_notFound() {
		UUID userId = UUID.randomUUID();
		when(userEntityRepository.findById(userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> userService.getUserByIdSystem(userId));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}

	@Test
	void getUserByEmailSystem_success() {
		UserEntity entity = UserEntity.builder()
				.id(UUID.randomUUID())
				.username("jailton")
				.email("jailton@email.com")
				.password("hashed")
				.build();

		when(userEntityRepository.findByEmail("jailton@email.com")).thenReturn(Optional.of(entity));

		UserModel model = userService.getUserByEmailSystem("jailton@email.com");

		assertEquals("jailton@email.com", model.getEmail());
	}

	@Test
	void getUserByEmailSystem_notFound() {
		when(userEntityRepository.findByEmail("missing@email.com")).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> userService.getUserByEmailSystem("missing@email.com"));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}
}
