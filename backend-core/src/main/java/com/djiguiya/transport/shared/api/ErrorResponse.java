package com.djiguiya.transport.shared.api;

import java.time.Instant;

/**
 * Standard error body for REST failures (see {@link com.djiguiya.transport.shared.exception.GlobalExceptionHandler}).
 */
public record ErrorResponse(
		String messageKey,
		String message,
		int status,
		Instant timestamp
) {
	/** Factory with server-side timestamp for client logging and support. */
	public static ErrorResponse of(String messageKey, String message, int status) {
		return new ErrorResponse(messageKey, message, status, Instant.now());
	}
}
