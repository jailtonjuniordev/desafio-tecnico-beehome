package com.jjdev.beehome_tecnico.auth.application.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jjdev.beehome_tecnico.auth.application.service.AuthService;
import com.jjdev.beehome_tecnico.auth.application.service.UserService;
import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.LoginResponseDTO;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtServiceUtils jwtServiceUtils;

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        UserModel user;
        try {
            user = userService.getUserByEmailSystem(request.email());
        } catch (CustomException ex) {
            if (ex.getHttpStatus().equals(HttpStatus.NOT_FOUND)) {
                throw new BadCredentialsException("Email or password incorrect");
            }
            throw ex;
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Email or password incorrect");
        }

        String accessToken = jwtServiceUtils.generate(user.getId(), user.getEmail());
        return LoginResponseDTO.builder()
                .token(accessToken)
                .tokenType("Bearer")
                .expiration(jwtServiceUtils.getExpirationMs())
                .build();
    }
}
