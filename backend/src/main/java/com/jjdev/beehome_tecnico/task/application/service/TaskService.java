package com.jjdev.beehome_tecnico.task.application.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

public interface TaskService {

	TaskResponseDTO create(TaskCreateRequestDTO request);

	TaskResponseDTO getById(UUID id);

	Page<TaskResponseDTO> list(
			TaskStatus status,
			String title,
			OffsetDateTime deadlineStart,
			OffsetDateTime deadlineEnd,
			Pageable pageable
	);

	TaskResponseDTO update(UUID id, TaskUpdateRequestDTO request);

	void delete(UUID id);
}
