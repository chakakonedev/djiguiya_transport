package com.djiguiya.transport.modules.auth.dto;

/**
 * Successful login payload: JWT access token plus identifiers needed by the client UI.
 *
 * @param accessToken bearer token for subsequent API calls
 * @param roleCode    application role (e.g. owner, manager) embedded in authorization rules
 */
public record LoginResponse(
		String accessToken,
		String userId,
		String email,
		String fullName,
		String roleCode
) {
}
