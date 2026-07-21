package com.jjdev.beehome_tecnico.task.infrastructure.persistence.mapper;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.jjdev.beehome_tecnico.task.domain.model.TaskModel;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.entity.TaskEntity;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

@Component
public class TaskMapper {

	public static TaskModel fromCreate(TaskCreateRequestDTO dto, UUID assignedTo) {
		if (dto == null) {
			return null;
		}
		return TaskModel.create(dto, assignedTo);
	}

	public static TaskModel fromUpdate(TaskModel current, TaskUpdateRequestDTO dto) {
		if (current == null || dto == null) {
			return null;
		}
		return current.update(dto);
	}

	public static TaskModel fromEntity(TaskEntity entity) {
		if (entity == null) {
			return null;
		}
		return TaskModel.builder()
				.id(entity.getId())
				.title(entity.getTitle().trim())
				.description(entity.getDescription())
				.status(entity.getStatus())
				.deadline(entity.getDeadline())
				.assignedTo(entity.getAssignedTo())
				.createdAt(entity.getCreatedAt())
				.updatedAt(entity.getUpdatedAt())
				.build();
	}

	public static TaskEntity toEntity(TaskModel model) {
		if (model == null) {
			return null;
		}
		return TaskEntity.builder()
				.id(model.getId())
				.title(model.getTitle().trim())
				.description(model.getDescription())
				.status(model.getStatus())
				.deadline(model.getDeadline())
				.assignedTo(model.getAssignedTo())
				.createdAt(model.getCreatedAt())
				.updatedAt(model.getUpdatedAt())
				.build();
	}

	public static TaskResponseDTO toResponse(TaskModel model) {
		if (model == null) {
			return null;
		}
		return TaskResponseDTO.builder()
				.id(model.getId())
				.title(model.getTitle().trim())
				.description(model.getDescription())
				.status(model.getStatus())
				.deadline(model.getDeadline())
				.assignedTo(model.getAssignedTo())
				.createdAt(model.getCreatedAt())
				.updatedAt(model.getUpdatedAt())
				.build();
	}
}
