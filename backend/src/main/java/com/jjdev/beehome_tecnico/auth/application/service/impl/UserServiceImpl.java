package com.jjdev.beehome_tecnico.auth.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import com.jjdev.beehome_tecnico.auth.application.service.UserService;
import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.entity.UserEntity;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.mapper.UserMapper;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.repository.UserEntityRepository;
import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

	private final UserEntityRepository userEntityRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtServiceUtils jwtServiceUtils;
	private static final String USER_NOT_FOUND_MESSAGE = "User not found";

	@Override
	@Transactional
	public UserResponseDTO create(UserCreateRequestDTO request) {
		if (userEntityRepository.existsByEmail(request.email())) {
			throw new CustomException("This email are already registered, try login into your account", HttpStatus.CONFLICT);
		}

		String hashedPassword = passwordEncoder.encode(request.password());
		UserModel created = UserMapper.fromCreate(request, hashedPassword);
		UserEntity saved = userEntityRepository.save(UserMapper.toEntity(created));
		return UserMapper.toResponse(UserMapper.fromEntity(saved));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponseDTO getLogged() {
		UserEntity entity = userEntityRepository.findById(jwtServiceUtils.getCurrentUserId())
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
		return UserMapper.toResponse(UserMapper.fromEntity(entity));
	}

	@Override
	@Transactional
	public UserResponseDTO update(UserUpdateRequestDTO request) {
		UserEntity entity = userEntityRepository.findById(jwtServiceUtils.getCurrentUserId())
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));

		UserModel current = UserMapper.fromEntity(entity);

		if (!current.getEmail().equals(request.email()) && userEntityRepository.existsByEmail(request.email())) {
			throw new CustomException("Email already registered", HttpStatus.CONFLICT);
		}

		String hashedPassword = null;
		if (StringUtils.isNotBlank(request.password())) {
			hashedPassword = passwordEncoder.encode(request.password());
		}

		UserModel updated = UserMapper.fromUpdate(current, request, hashedPassword);
		UserEntity saved = userEntityRepository.save(UserMapper.toEntity(updated));
		return UserMapper.toResponse(UserMapper.fromEntity(saved));
	}

	@Override
	@Transactional(readOnly = true)
	public UserModel getUserByIdSystem(UUID id) {
		return userEntityRepository.findById(id)
				.map(UserMapper::fromEntity)
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
	}

	@Override
	@Transactional(readOnly = true)
	public UserModel getUserByEmailSystem(String email) {
		return userEntityRepository.findByEmail(email)
				.map(UserMapper::fromEntity)
				.orElseThrow(() -> new CustomException(USER_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
	}
}
