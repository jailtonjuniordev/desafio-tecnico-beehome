package com.jjdev.beehome_tecnico.task.application.service.impl;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;
import com.jjdev.beehome_tecnico.task.application.service.TaskService;
import com.jjdev.beehome_tecnico.task.domain.model.TaskModel;
import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.entity.TaskEntity;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.mapper.TaskMapper;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.repository.TaskEntityRepository;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

	private static final String TASK_NOT_FOUND_MESSAGE = "Task not found";
	private static final String TITLE_ALREADY_EXISTS_MESSAGE = "Task title already exists";
	private static final String DEADLINE_IN_PAST_MESSAGE = "Deadline cannot be in the past";
	private static final String DEADLINE_RANGE_INVALID_MESSAGE = "deadlineStart must be before or equal to deadlineEnd";

	private final TaskEntityRepository taskEntityRepository;
	private final JwtServiceUtils jwtServiceUtils;

	@Override
	@Transactional
	public TaskResponseDTO create(TaskCreateRequestDTO request) {
		UUID currentUserId = jwtServiceUtils.getCurrentUserId();
		validateDeadline(request.deadline());

		if (taskEntityRepository.existsByAssignedToAndTitle(currentUserId, request.title())) {
			throw new CustomException(TITLE_ALREADY_EXISTS_MESSAGE, HttpStatus.CONFLICT);
		}

		TaskModel created = TaskMapper.fromCreate(request, currentUserId);
		TaskEntity saved = taskEntityRepository.save(TaskMapper.toEntity(created));
		return TaskMapper.toResponse(TaskMapper.fromEntity(saved));
	}

	@Override
	@Transactional(readOnly = true)
	public TaskResponseDTO getById(UUID id) {
		TaskEntity entity = findOwnedTask(id);
		return TaskMapper.toResponse(TaskMapper.fromEntity(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<TaskResponseDTO> list(
			TaskStatus status,
			String title,
			OffsetDateTime deadlineStart,
			OffsetDateTime deadlineEnd,
			Pageable pageable
	) {
		if (deadlineStart != null && deadlineEnd != null && deadlineStart.isAfter(deadlineEnd)) {
			throw new CustomException(DEADLINE_RANGE_INVALID_MESSAGE, HttpStatus.BAD_REQUEST);
		}

		UUID currentUserId = jwtServiceUtils.getCurrentUserId();

		Map<String, Object> filters = new HashMap<>();
		filters.put("assignedTo", currentUserId);
		if (status != null) {
			filters.put("status", status);
		}
		if (StringUtils.hasText(title)) {
			filters.put("title", title);
		}

		Map<String, Object> deadlineRange = new HashMap<>();
		if (deadlineStart != null) {
			deadlineRange.put("startDate", deadlineStart);
		}
		if (deadlineEnd != null) {
			deadlineRange.put("endDate", deadlineEnd);
		}
		if (!deadlineRange.isEmpty()) {
			filters.put("deadline", deadlineRange);
		}

		return taskEntityRepository.dynamicSearchFilters(filters, pageable)
				.map(entity -> TaskMapper.toResponse(TaskMapper.fromEntity(entity)));
	}

	@Override
	@Transactional
	public TaskResponseDTO update(UUID id, TaskUpdateRequestDTO request) {
		validateDeadline(request.deadline());

		TaskEntity entity = findOwnedTask(id);
		TaskModel current = TaskMapper.fromEntity(entity);

		if (!current.getTitle().equals(request.title())
				&& taskEntityRepository.existsByAssignedToAndTitle(current.getAssignedTo(), request.title())) {
			throw new CustomException(TITLE_ALREADY_EXISTS_MESSAGE, HttpStatus.CONFLICT);
		}

		TaskModel updated = TaskMapper.fromUpdate(current, request);
		TaskEntity saved = taskEntityRepository.save(TaskMapper.toEntity(updated));
		return TaskMapper.toResponse(TaskMapper.fromEntity(saved));
	}

	@Override
	@Transactional
	public void delete(UUID id) {
		TaskEntity entity = findOwnedTask(id);
		taskEntityRepository.delete(entity);
	}

	private TaskEntity findOwnedTask(UUID id) {
		UUID currentUserId = jwtServiceUtils.getCurrentUserId();
		return taskEntityRepository.findByIdAndAssignedTo(id, currentUserId)
				.orElseThrow(() -> new CustomException(TASK_NOT_FOUND_MESSAGE, HttpStatus.NOT_FOUND));
	}

	private void validateDeadline(OffsetDateTime deadline) {
		if (deadline.isBefore(OffsetDateTime.now())) {
			throw new CustomException(DEADLINE_IN_PAST_MESSAGE, HttpStatus.BAD_REQUEST);
		}
	}
}
