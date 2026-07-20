package com.jjdev.beehome_tecnico.auth.application.service;

import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;

public interface UserService {

	UserResponseDTO create(UserCreateRequestDTO request);

	UserResponseDTO getLogged();

	UserResponseDTO update(UserUpdateRequestDTO request);
}
