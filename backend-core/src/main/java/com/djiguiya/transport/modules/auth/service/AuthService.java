package com.djiguiya.transport.modules.auth.service;

import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.auth.dto.LoginResponse;
import com.djiguiya.transport.modules.identity.entities.UserEntity;
import com.djiguiya.transport.modules.identity.repository.UserRepository;
import com.djiguiya.transport.shared.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

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
}
