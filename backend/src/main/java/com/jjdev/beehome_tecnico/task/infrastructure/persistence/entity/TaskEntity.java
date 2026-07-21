package com.jjdev.beehome_tecnico.task.infrastructure.persistence.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.jjdev.beehome_tecnico.shared.infrastructure.persistence.entity.BaseEntity;
import com.jjdev.beehome_tecnico.task.domain.model.TaskStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class TaskEntity extends BaseEntity {

	@Column(name = "title", nullable = false, length = 255)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private TaskStatus status;

	@Column(name = "deadline", nullable = false)
	private OffsetDateTime deadline;

	@Column(name = "assigned_to", nullable = false, length = 36)
	@JdbcTypeCode(SqlTypes.CHAR)
	private UUID assignedTo;
}
