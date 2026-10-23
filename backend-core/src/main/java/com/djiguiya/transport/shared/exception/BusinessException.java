package com.djiguiya.transport.shared.exception;

import org.springframework.http.HttpStatus;

/**
 * Domain/business failure raised by services when a rule is violated
 * (e.g. entity not found, invalid state).
 * <p>
 * Carries an i18n {@code messageKey} (resolved by {@link GlobalExceptionHandler})
 * and the HTTP status the API should return — so controllers stay thin.
 */
public class BusinessException extends RuntimeException {

	/** Message bundle key, e.g. {@code staff.not_found}. */
	private final String messageKey;

	/** HTTP status mapped for the client response. */
	private final HttpStatus status;

	/** {@code messageKey} is resolved to a localized message in {@link GlobalExceptionHandler}. */
	public BusinessException(String messageKey, HttpStatus status) {
		super(messageKey);
		this.messageKey = messageKey;
		this.status = status;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public HttpStatus getStatus() {
		return status;
	}
}
