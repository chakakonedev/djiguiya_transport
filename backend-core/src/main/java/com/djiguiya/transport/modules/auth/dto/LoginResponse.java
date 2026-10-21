package com.djiguiya.transport.modules.auth.dto;

public record LoginResponse(
		String accessToken,
		String userId,
		String email,
		String fullName,
		String roleCode
) {
}
