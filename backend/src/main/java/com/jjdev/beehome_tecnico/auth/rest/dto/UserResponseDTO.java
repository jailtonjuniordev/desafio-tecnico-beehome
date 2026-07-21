package com.jjdev.beehome_tecnico.auth.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;

@Builder
public record UserResponseDTO(
		UUID id,
		String username,
		String email,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {}