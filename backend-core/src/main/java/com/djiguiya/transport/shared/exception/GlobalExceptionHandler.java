package com.djiguiya.transport.shared.exception;

import com.djiguiya.transport.shared.api.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Central API error translator: turns exceptions into a uniform {@link ErrorResponse}
 * with localized messages. Controllers/services throw; this class shapes HTTP bodies.
 * <p>
 * Handler order matters conceptually: specific types first
 * ({@link BusinessException}, {@link ResponseStatusException}, validation),
 * then a catch-all {@link Exception} for unexpected failures.
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

	private final MessageSource messageSource;

	/** Maps a known business rule failure to its status + translated message. */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
		return build(ex.getMessageKey(), ex.getStatus());
	}

	/**
	 * Spring {@link ResponseStatusException}: uses {@code reason} as message key when present,
	 * otherwise falls back to {@code common.not_found}.
	 */
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ErrorResponse> handleStatus(ResponseStatusException ex) {
		String key = ex.getReason() != null ? ex.getReason() : "common.not_found";
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		return build(key, status);
	}

	/**
	 * Bean validation failures ({@code @Valid}): 400 with field names appended
	 * so the client knows which inputs failed.
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		String details = ex.getBindingResult().getFieldErrors().stream()
				.map(FieldError::getField)
				.collect(Collectors.joining(", "));
		Locale locale = LocaleContextHolder.getLocale();
		String message = messageSource.getMessage("common.validation_failed", null, locale)
				+ (details.isEmpty() ? "" : ": " + details);
		ErrorResponse body = ErrorResponse.of("common.validation_failed", message, HttpStatus.BAD_REQUEST.value());
		return ResponseEntity.badRequest().body(body);
	}

	/**
	 * Last-resort handler: 500 with a stable key; message is the exception text
	 * or a generic fallback when null (avoids leaking null to the client).
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
		ErrorResponse body = ErrorResponse.of(
				"common.internal_error",
				ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
				HttpStatus.INTERNAL_SERVER_ERROR.value());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	/** Resolves {@code messageKey} via MessageSource for the current request locale. */
	private ResponseEntity<ErrorResponse> build(String messageKey, HttpStatus status) {
		Locale locale = LocaleContextHolder.getLocale();
		String message = messageSource.getMessage(messageKey, null, messageKey, locale);
		return ResponseEntity.status(status).body(ErrorResponse.of(messageKey, message, status.value()));
	}
}
