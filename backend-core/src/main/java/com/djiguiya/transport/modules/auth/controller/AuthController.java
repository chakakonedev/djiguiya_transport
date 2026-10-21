package com.djiguiya.transport.modules.auth.controller;

import com.djiguiya.transport.modules.auth.dto.ChangePasswordRequest;
import com.djiguiya.transport.modules.auth.dto.LoginRequest;
import com.djiguiya.transport.modules.auth.dto.LoginResponse;
import com.djiguiya.transport.modules.auth.service.AuthService;
import com.djiguiya.transport.shared.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.ok("auth.login_success", authService.login(request));
	}

	@PostMapping("/change-password")
	public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
		authService.changePassword(request);
		return ApiResponse.ok("auth.password_changed", null);
	}
}
