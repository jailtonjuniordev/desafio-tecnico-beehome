package com.jjdev.beehome_tecnico.auth.infrastructure.persistence.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jjdev.beehome_tecnico.auth.infrastructure.persistence.entity.UserEntity;

public interface UserEntityRepository extends JpaRepository<UserEntity, UUID> {
	boolean existsByEmail(String email);
}
