package com.djiguiya.transport.shared.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.mock;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

	@Mock
	private MessageSource messageSource;
	@InjectMocks
	private GlobalExceptionHandler handler;

	@Test
	void handleBusiness() {
		when(messageSource.getMessage(eq("staff.not_found"), any(), eq("staff.not_found"), any(Locale.class)))
				.thenReturn("Not found");
		var response = handler.handleBusiness(new BusinessException("staff.not_found", HttpStatus.NOT_FOUND));
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertEquals("staff.not_found", response.getBody().messageKey());
	}

	@Test
	void handleStatus_withReason() {
		when(messageSource.getMessage(eq("auth.invalid_credentials"), any(), eq("auth.invalid_credentials"), any(Locale.class)))
				.thenReturn("Bad creds");
		var response = handler.handleStatus(
				new ResponseStatusException(HttpStatus.UNAUTHORIZED, "auth.invalid_credentials"));
		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertEquals("auth.invalid_credentials", response.getBody().messageKey());
	}

	@Test
	void handleStatus_withoutReason() {
		when(messageSource.getMessage(eq("common.not_found"), any(), eq("common.not_found"), any(Locale.class)))
				.thenReturn("Missing");
		var response = handler.handleStatus(new ResponseStatusException(HttpStatus.NOT_FOUND));
		assertEquals("common.not_found", response.getBody().messageKey());
	}

	@Test
	void handleValidation_listsFields() throws Exception {
		when(messageSource.getMessage(eq("common.validation_failed"), any(), any(Locale.class)))
				.thenReturn("Validation failed");
		Object target = new Object();
		BeanPropertyBindingResult binding = new BeanPropertyBindingResult(target, "request");
		binding.addError(new FieldError("request", "fullName", "required"));
		MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mock(MethodParameter.class), binding);

		var response = handler.handleValidation(ex);
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		assertEquals("common.validation_failed", response.getBody().messageKey());
		assertEquals(true, response.getBody().message().contains("fullName"));
	}

	@Test
	void handleGeneric_usesExceptionMessage() {
		var response = handler.handleGeneric(new RuntimeException("boom"));
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertEquals("common.internal_error", response.getBody().messageKey());
		assertEquals("boom", response.getBody().message());
	}

	@Test
	void handleGeneric_nullMessageFallback() {
		var response = handler.handleGeneric(new RuntimeException());
		assertEquals("Unexpected error", response.getBody().message());
	}
}
