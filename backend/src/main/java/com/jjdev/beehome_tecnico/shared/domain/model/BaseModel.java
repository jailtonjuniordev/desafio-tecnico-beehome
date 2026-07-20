package com.jjdev.beehome_tecnico.shared.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseModel {

	private UUID id;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;
}
