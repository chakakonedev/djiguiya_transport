package com.djiguiya.transport.modules.identity.repository;

import com.djiguiya.transport.modules.identity.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, String> {
	Optional<UserEntity> findByEmailIgnoreCase(String email);
}
