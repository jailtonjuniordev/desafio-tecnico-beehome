package com.jjdev.beehome_tecnico.auth.infrastructure.persistence.mapper;

import com.jjdev.beehome_tecnico.auth.domain.model.UserModel;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.entity.UserEntity;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserCreateRequestDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserResponseDTO;
import com.jjdev.beehome_tecnico.auth.rest.dto.UserUpdateRequestDTO;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static UserModel fromCreate(UserCreateRequestDTO dto, String hashedPassword) {
        if (dto == null) {
            return null;
        }
        return UserModel.create(dto.username(), dto.email(), hashedPassword);
    }

    public static UserModel fromUpdate(UserModel current, UserUpdateRequestDTO dto, String hashedPassword) {
        if (current == null || dto == null) {
            return null;
        }
        return current.update(dto.username(), dto.email(), hashedPassword);
    }

    public static UserModel fromEntity(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return UserModel.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserEntity toEntity(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        return UserEntity.builder()
                .id(userModel.getId())
                .username(userModel.getUsername())
                .email(userModel.getEmail())
                .password(userModel.getPassword())
                .createdAt(userModel.getCreatedAt())
                .updatedAt(userModel.getUpdatedAt())
                .build();
    }

    public static UserResponseDTO toResponse(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        return UserResponseDTO.builder()
                .id(userModel.getId())
                .username(userModel.getUsername())
                .email(userModel.getEmail())
                .createdAt(userModel.getCreatedAt())
                .updatedAt(userModel.getUpdatedAt())
                .build();
    }
}
