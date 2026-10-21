package com.djiguiya.transport.shared.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

	private final String messageKey;
	private final HttpStatus status;

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
