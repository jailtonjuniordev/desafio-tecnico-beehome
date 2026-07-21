package com.jjdev.beehome_tecnico.auth.application.service;

import com.jjdev.beehome_tecnico.auth.rest.dto.LoginRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginResponseDTO;

public interface AuthService {

	LoginResponseDTO login(LoginRequestDTO request);
}
