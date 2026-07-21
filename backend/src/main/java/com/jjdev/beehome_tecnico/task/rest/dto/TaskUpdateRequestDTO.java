package com.jjdev.beehome_tecnico.task.rest.dto;

import java.time.OffsetDateTime;

import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;

import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record TaskUpdateRequestDTO(
		@Size(max = 255)
		String title,

		String description,

		TaskStatus status,

		OffsetDateTime deadline
) {
}
