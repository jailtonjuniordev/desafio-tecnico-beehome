package com.jjdev.beehome_tecnico.auth.rest.dto;

import lombok.Builder;

@Builder
public record LoginResponseDTO(
		String token,
		String tokenType,
		long expiration
) {
}
