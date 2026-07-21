package com.jjdev.beehome_tecnico.task.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.jjdev.beehome_tecnico.shared.domain.model.BaseModel;

import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskModel extends BaseModel {

	private String title;
	private String description;
	private TaskStatus status;
	private OffsetDateTime deadline;
	private UUID assignedTo;

	public static TaskModel create(TaskCreateRequestDTO dto, UUID assignedTo) {
		return TaskModel.builder()
				.title(dto.title().trim())
				.description(dto.description().trim())
				.status(dto.status())
				.deadline(dto.deadline())
				.assignedTo(assignedTo)
				.build();
	}

	public TaskModel update(TaskUpdateRequestDTO dto) {
		return TaskModel.builder()
				.id(getId())
				.title(StringUtils.isNotBlank(dto.title().trim()) ? dto.title().trim() : getTitle())
				.description(StringUtils.isNotBlank(dto.description().trim()) ? dto.description().trim() : getDescription())
				.status(dto.status() != null ? dto.status() : getStatus())
				.deadline(dto.deadline() != null ? dto.deadline() : getDeadline())
				.assignedTo(getAssignedTo())
				.createdAt(getCreatedAt())
				.updatedAt(getUpdatedAt())
				.build();
	}
}
