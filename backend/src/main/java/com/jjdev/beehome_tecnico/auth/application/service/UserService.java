package com.jjdev.beehome_tecnico.auth.application.service;

import java.util.UUID;

import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;

public interface UserService {

	UserResponseDTO create(UserCreateRequestDTO request);

	UserResponseDTO getLogged();

	UserResponseDTO update(UserUpdateRequestDTO request);

	UserModel getUserByIdSystem(UUID id);

	UserModel getUserByEmailSystem(String email);
}
