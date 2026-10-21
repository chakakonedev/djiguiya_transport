package com.djiguiya.transport.shared.api;

import java.time.Instant;

public record ErrorResponse(
		String messageKey,
		String message,
		int status,
		Instant timestamp
) {
	public static ErrorResponse of(String messageKey, String message, int status) {
		return new ErrorResponse(messageKey, message, status, Instant.now());
	}
}
