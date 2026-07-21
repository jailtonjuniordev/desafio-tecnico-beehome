package com.jjdev.beehome_tecnico.task.rest.dto;

import java.time.OffsetDateTime;

import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TaskCreateRequestDTO(
		@NotBlank
		@Size(max = 255)
		String title,

		String description,

		@NotNull
		TaskStatus status,

		@NotNull
		OffsetDateTime deadline
) {
}
