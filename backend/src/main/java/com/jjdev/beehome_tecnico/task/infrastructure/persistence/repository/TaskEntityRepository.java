package com.jjdev.beehome_tecnico.task.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository.BaseRepository;
import com.jjdev.beehome_tecnico.task.infrastructure.persistence.entity.TaskEntity;

public interface TaskEntityRepository extends BaseRepository<TaskEntity, UUID> {

	boolean existsByAssignedToAndTitle(UUID assignedTo, String title);

	Optional<TaskEntity> findByIdAndAssignedTo(UUID id, UUID assignedTo);
}
