package com.djiguiya.transport.support;

import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.UUID;

public final class TestUserFactory {

	private TestUserFactory() {
	}

	public static UserEntity createOwner(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		UserEntity user = new UserEntity();
		user.setId(UUID.randomUUID().toString());
		user.setEmail("owner@test.local");
		user.setPasswordHash(passwordEncoder.encode("password"));
		user.setFullName("Test Owner");
		user.setRoleCode("owner");
		user.setActive(true);
		user.setCreatedAt(Instant.now());
		user.setUpdatedAt(Instant.now());
		return userRepository.save(user);
	}
}
