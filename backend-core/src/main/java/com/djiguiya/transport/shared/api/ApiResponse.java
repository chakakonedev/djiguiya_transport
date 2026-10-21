package com.djiguiya.transport.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(boolean success, String messageKey, T data) {

	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(true, null, data);
	}

	public static <T> ApiResponse<T> ok(String messageKey, T data) {
		return new ApiResponse<>(true, messageKey, data);
	}

	public static ApiResponse<Void> fail(String messageKey) {
		return new ApiResponse<>(false, messageKey, null);
	}
}
