package com.djiguiya.transport.modules.auth.service;

import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.shared.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService#login}: mocked repositories and JWT issuance.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@InjectMocks
	private AuthService authService;

	/** Returns an access token when credentials and active user are valid. */
	@Test
	void login_success() {
		UserEntity user = new UserEntity();
		user.setId("u1");
		user.setEmail("a@test.com");
		user.setPasswordHash("hash");
		user.setFullName("A");
		user.setRoleCode("owner");
		user.setActive(true);

		when(userRepository.findByEmailIgnoreCase("a@test.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("password", "hash")).thenReturn(true);
		when(jwtService.generateAccessToken("u1", "a@test.com", "owner")).thenReturn("token");

		var response = authService.login(new LoginRequest("a@test.com", "password"));
		assertEquals("token", response.accessToken());
	}

	/** Rejects login when the password does not match the stored hash. */
	@Test
	void login_invalidPassword() {
		UserEntity user = new UserEntity();
		user.setActive(true);
		user.setPasswordHash("hash");
		when(userRepository.findByEmailIgnoreCase(any())).thenReturn(Optional.of(user));
		when(passwordEncoder.matches(any(), any())).thenReturn(false);

		assertThrows(ResponseStatusException.class,
				() -> authService.login(new LoginRequest("a@test.com", "wrong")));
	}
}
