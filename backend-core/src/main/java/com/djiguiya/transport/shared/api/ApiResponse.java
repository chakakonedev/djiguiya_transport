package com.djiguiya.transport.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Envelope for successful API payloads; {@code messageKey} is optional i18n hint for the client.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String messageKey, T data) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, data);
	}

	public static <T> ApiResponse<T> ok(String messageKey, T data) {
		return new ApiResponse<>(true, messageKey, data);
	}

	/** Explicit failure envelope when controllers return errors without throwing. */
	public static ApiResponse<Void> fail(String messageKey) {
		return new ApiResponse<>(false, messageKey, null);
	}
}
