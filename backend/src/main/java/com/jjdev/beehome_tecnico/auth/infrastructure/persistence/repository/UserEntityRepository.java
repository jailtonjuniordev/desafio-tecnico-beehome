package com.jjdev.beehome_tecnico.auth.infrastructure.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import com.jjdev.beehome_tecnico.shared.infrastructure.persistence.repository.BaseRepository;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.entity.UserEntity;

public interface UserEntityRepository extends BaseRepository<UserEntity, UUID> {
	boolean existsByEmail(String email);

	Optional<UserEntity> findByEmail(String email);
}
