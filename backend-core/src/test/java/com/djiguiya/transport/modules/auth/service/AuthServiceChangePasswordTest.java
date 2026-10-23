package com.djiguiya.transport.modules.auth.service;

import com.djiguiya.transport.modules.auth.dto.ChangePasswordRequest;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.shared.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link AuthService#changePassword}: security context and password rules with mocks.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceChangePasswordTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private JwtService jwtService;
	@InjectMocks
	private AuthService authService;

	@AfterEach
	void clearSecurity() {
		SecurityContextHolder.clearContext();
	}

	/** Persists a new hash and marks password changed when current password is correct. */
	@Test
	void changePassword_success() {
		authenticate("u1");
		UserEntity user = activeUser("u1", "hash");
		when(userRepository.findById("u1")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("old", "hash")).thenReturn(true);
		when(passwordEncoder.matches("new-pass", "hash")).thenReturn(false);
		when(passwordEncoder.encode("new-pass")).thenReturn("new-hash");
		when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		authService.changePassword(new ChangePasswordRequest("old", "new-pass"));

		assertTrue(user.isPasswordChanged());
		verify(userRepository).save(user);
	}

	/** Fails when the supplied current password does not match. */
	@Test
	void changePassword_rejectsWrongCurrent() {
		authenticate("u1");
		when(userRepository.findById("u1")).thenReturn(Optional.of(activeUser("u1", "hash")));
		when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

		assertThrows(ResponseStatusException.class,
				() -> authService.changePassword(new ChangePasswordRequest("bad", "new-pass")));
	}

	/** Fails when the new password equals the current password. */
	@Test
	void changePassword_rejectsSamePassword() {
		authenticate("u1");
		when(userRepository.findById("u1")).thenReturn(Optional.of(activeUser("u1", "hash")));
		when(passwordEncoder.matches("same", "hash")).thenReturn(true);

		assertThrows(ResponseStatusException.class,
				() -> authService.changePassword(new ChangePasswordRequest("same", "same")));
	}

	/** Fails when no authenticated user is present in the security context. */
	@Test
	void changePassword_requiresAuthentication() {
		SecurityContextHolder.clearContext();
		assertThrows(ResponseStatusException.class,
				() -> authService.changePassword(new ChangePasswordRequest("a", "b")));
	}

	private static void authenticate(String userId) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(userId, null, List.of()));
	}

	private static UserEntity activeUser(String id, String hash) {
		UserEntity user = new UserEntity();
		user.setId(id);
		user.setPasswordHash(hash);
		user.setActive(true);
		return user;
	}
}
