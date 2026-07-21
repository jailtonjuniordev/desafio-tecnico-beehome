package com.jjdev.beehome_tecnico.task.rest.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;

import lombok.Builder;

@Builder
public record TaskResponseDTO(
		UUID id,
		String title,
		String description,
		TaskStatus status,
		OffsetDateTime deadline,
		UUID assignedTo,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
) {
}
