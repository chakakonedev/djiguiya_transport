package com.djiguiya.transport.modules.auth.service;

import com.djiguiya.transport.modules.auth.dto.ChangePasswordRequest;
import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.auth.dto.LoginResponse;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.shared.security.JwtService;
import com.djiguiya.transport.shared.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

/**
 * Authenticates users against stored credentials and manages password updates for active accounts.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	/**
	 * Validates email/password for an active user and issues a signed access token.
	 *
	 * @throws ResponseStatusException 401 when credentials are invalid or the account is inactive
	 */
	public LoginResponse login(LoginRequest request) {
		UserEntity user = userRepository.findByEmailIgnoreCase(request.email().trim())
				.filter(UserEntity::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials"));

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials");
		}

		String token = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRoleCode());
		return new LoginResponse(token, user.getId(), user.getEmail(), user.getFullName(), user.getRoleCode());
	}

	/**
	 * Changes the password for the currently authenticated user and marks {@code passwordChanged}.
	 *
	 * @throws ResponseStatusException 400 when current password is wrong or new password equals the old one
	 */
	@Transactional
	public void changePassword(ChangePasswordRequest request) {
		String userId = SecurityUtils.currentUserId()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials"));

		UserEntity user = userRepository.findById(userId)
				.filter(UserEntity::isActive)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials"));

		if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.current_password_invalid");
		}

		String newPassword = request.newPassword().trim();
		if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "auth.new_password_same");
		}

		user.setPasswordHash(passwordEncoder.encode(newPassword));
		user.setPasswordChanged(true);
		user.setUpdatedAt(Instant.now());
		userRepository.save(user);
	}
}
