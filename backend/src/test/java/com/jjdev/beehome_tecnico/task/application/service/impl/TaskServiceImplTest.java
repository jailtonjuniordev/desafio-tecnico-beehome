package com.jjdev.beehome_tecnico.task.application.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import com.jjdev.beehome_tecnico.auth.infrastructure.security.JwtServiceUtils;
import com.jjdev.beehome_tecnico.shared.domain.exception.CustomException;
import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.entity.TaskEntity;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.repository.TaskEntityRepository;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskCreateRequestDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskResponseDTO;
import com.jjdev.beehome_tecnico.task.rest.dto.TaskUpdateRequestDTO;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

	@Mock
	private TaskEntityRepository taskEntityRepository;

	@Mock
	private JwtServiceUtils jwtServiceUtils;

	@InjectMocks
	private TaskServiceImpl taskService;

	@Test
	void create_success() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		OffsetDateTime deadline = OffsetDateTime.now().plusDays(7);
		TaskCreateRequestDTO request = TaskCreateRequestDTO.builder()
				.title("Task A")
				.description("Desc")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.build();
		TaskEntity saved = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.description("Desc")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.assignedTo(userId)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.existsByAssignedToAndTitle(userId, "Task A")).thenReturn(false);
		when(taskEntityRepository.save(any(TaskEntity.class))).thenReturn(saved);

		TaskResponseDTO response = taskService.create(request);

		assertEquals(taskId, response.id());
		assertEquals("Task A", response.title());
		assertEquals(userId, response.assignedTo());
		verify(taskEntityRepository).save(any(TaskEntity.class));
	}

	@Test
	void create_duplicateTitle() {
		UUID userId = UUID.randomUUID();
		TaskCreateRequestDTO request = TaskCreateRequestDTO.builder()
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(OffsetDateTime.now().plusDays(7))
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.existsByAssignedToAndTitle(userId, "Task A")).thenReturn(true);

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.create(request));
		assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).save(any());
	}

	@Test
	void create_deadlineInPast() {
		UUID userId = UUID.randomUUID();
		TaskCreateRequestDTO request = TaskCreateRequestDTO.builder()
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(OffsetDateTime.now().minusDays(1))
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.create(request));
		assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).save(any());
	}

	@Test
	void getById_success() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		OffsetDateTime deadline = OffsetDateTime.now().plusDays(7);
		TaskEntity entity = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.assignedTo(userId)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.of(entity));

		TaskResponseDTO response = taskService.getById(taskId);

		assertEquals(taskId, response.id());
		assertEquals("Task A", response.title());
	}

	@Test
	void getById_notFound() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.getById(taskId));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}

	@Test
	@SuppressWarnings("unchecked")
	void list_success() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		OffsetDateTime deadline = OffsetDateTime.now().plusDays(7);
		OffsetDateTime start = OffsetDateTime.now().plusDays(1);
		OffsetDateTime end = OffsetDateTime.now().plusDays(30);
		Pageable pageable = PageRequest.of(0, 10);
		TaskEntity entity = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.assignedTo(userId)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.dynamicSearchFilters(any(Map.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(entity), pageable, 1));

		Page<TaskResponseDTO> page = taskService.list(TaskStatus.PENDING, "Task", start, end, pageable);

		assertEquals(1, page.getTotalElements());
		assertEquals("Task A", page.getContent().getFirst().title());

		ArgumentCaptor<Map<String, Object>> filtersCaptor = ArgumentCaptor.forClass(Map.class);
		verify(taskEntityRepository).dynamicSearchFilters(filtersCaptor.capture(), eq(pageable));
		Map<String, Object> filters = filtersCaptor.getValue();
		assertEquals(userId, filters.get("assignedTo"));
		assertEquals(TaskStatus.PENDING, filters.get("status"));
		assertEquals("Task", filters.get("title"));
		Map<String, Object> deadlineRange = (Map<String, Object>) filters.get("deadline");
		assertEquals(start, deadlineRange.get("startDate"));
		assertEquals(end, deadlineRange.get("endDate"));
	}

	@Test
	void list_invalidDeadlineRange() {
		OffsetDateTime start = OffsetDateTime.now().plusDays(10);
		OffsetDateTime end = OffsetDateTime.now().plusDays(1);
		Pageable pageable = PageRequest.of(0, 10);

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.list(null, null, start, end, pageable));
		assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).dynamicSearchFilters(any(), any());
	}

	@Test
	void update_success() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		OffsetDateTime deadline = OffsetDateTime.now().plusDays(7);
		TaskEntity entity = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.description("Desc")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.assignedTo(userId)
				.build();
		TaskUpdateRequestDTO request = TaskUpdateRequestDTO.builder()
				.title("Task B")
				.description("Desc 2")
				.status(TaskStatus.IN_PROGRESS)
				.deadline(deadline.plusDays(1))
				.build();
		TaskEntity saved = TaskEntity.builder()
				.id(taskId)
				.title("Task B")
				.description("Desc 2")
				.status(TaskStatus.IN_PROGRESS)
				.deadline(deadline.plusDays(1))
				.assignedTo(userId)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.of(entity));
		when(taskEntityRepository.existsByAssignedToAndTitle(userId, "Task B")).thenReturn(false);
		when(taskEntityRepository.save(any(TaskEntity.class))).thenReturn(saved);

		TaskResponseDTO response = taskService.update(taskId, request);

		assertEquals("Task B", response.title());
		assertEquals(TaskStatus.IN_PROGRESS, response.status());
	}

	@Test
	void update_duplicateTitle() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		OffsetDateTime deadline = OffsetDateTime.now().plusDays(7);
		TaskEntity entity = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.assignedTo(userId)
				.build();
		TaskUpdateRequestDTO request = TaskUpdateRequestDTO.builder()
				.title("Task B")
				.status(TaskStatus.PENDING)
				.deadline(deadline)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.of(entity));
		when(taskEntityRepository.existsByAssignedToAndTitle(userId, "Task B")).thenReturn(true);

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.update(taskId, request));
		assertEquals(HttpStatus.CONFLICT, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).save(any());
	}

	@Test
	void update_notFound() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		TaskUpdateRequestDTO request = TaskUpdateRequestDTO.builder()
				.title("Task B")
				.status(TaskStatus.PENDING)
				.deadline(OffsetDateTime.now().plusDays(7))
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.update(taskId, request));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
	}

	@Test
	void update_deadlineInPast() {
		UUID taskId = UUID.randomUUID();
		TaskUpdateRequestDTO request = TaskUpdateRequestDTO.builder()
				.title("Task B")
				.status(TaskStatus.PENDING)
				.deadline(OffsetDateTime.now().minusDays(1))
				.build();

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.update(taskId, request));
		assertEquals(HttpStatus.BAD_REQUEST, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).findByIdAndAssignedTo(any(), any());
	}

	@Test
	void delete_success() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();
		TaskEntity entity = TaskEntity.builder()
				.id(taskId)
				.title("Task A")
				.status(TaskStatus.PENDING)
				.deadline(OffsetDateTime.now().plusDays(7))
				.assignedTo(userId)
				.build();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.of(entity));

		taskService.delete(taskId);

		verify(taskEntityRepository).delete(entity);
	}

	@Test
	void delete_notFound() {
		UUID userId = UUID.randomUUID();
		UUID taskId = UUID.randomUUID();

		when(jwtServiceUtils.getCurrentUserId()).thenReturn(userId);
		when(taskEntityRepository.findByIdAndAssignedTo(taskId, userId)).thenReturn(Optional.empty());

		CustomException thrown = assertThrows(CustomException.class, () -> taskService.delete(taskId));
		assertEquals(HttpStatus.NOT_FOUND, thrown.getHttpStatus());
		verify(taskEntityRepository, never()).delete(any());
	}
}
