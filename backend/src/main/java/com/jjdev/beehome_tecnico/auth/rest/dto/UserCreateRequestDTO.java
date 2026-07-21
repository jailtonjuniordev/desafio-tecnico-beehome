package com.jjdev.beehome_tecnico.auth.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record UserCreateRequestDTO(
		@NotBlank
		@Size(max = 100)
		String username,

		@NotBlank
		@Email
		@Size(max = 255)
		String email,

		@NotBlank
		@Size(min = 8, max = 72)
		String password
) {
}
