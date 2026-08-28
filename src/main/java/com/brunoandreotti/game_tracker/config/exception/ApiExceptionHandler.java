package com.brunoandreotti.game_tracker.config.exception;

import com.brunoandreotti.game_tracker.adapter.in.web.ApiErrorResponse;
import com.brunoandreotti.game_tracker.core.exception.CatalogUnavailableException;
import com.brunoandreotti.game_tracker.core.exception.DuplicateRawgIdException;
import com.brunoandreotti.game_tracker.core.exception.GameNotFoundException;
import com.brunoandreotti.game_tracker.core.exception.InvalidDurationException;
import com.brunoandreotti.game_tracker.core.exception.InvalidPatchRequestException;
import com.brunoandreotti.game_tracker.core.exception.PlaySessionNotFoundException;
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler({
			ConstraintViolationException.class,
			MethodArgumentNotValidException.class,
			HandlerMethodValidationException.class,
			MissingServletRequestParameterException.class,
			HttpMessageNotReadableException.class,
			InvalidPatchRequestException.class,
			InvalidDurationException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
		return response(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler({
			GameNotFoundException.class,
			TrackedGameNotFoundException.class,
			PlaySessionNotFoundException.class
	})
	public ResponseEntity<ApiErrorResponse> handleNotFound(RuntimeException exception) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage());
	}

	@ExceptionHandler(DuplicateRawgIdException.class)
	public ResponseEntity<ApiErrorResponse> handleConflict(DuplicateRawgIdException exception) {
		return response(HttpStatus.CONFLICT, exception.getMessage());
	}

	@ExceptionHandler(CatalogUnavailableException.class)
	public ResponseEntity<ApiErrorResponse> handleCatalogUnavailable(CatalogUnavailableException exception) {
		return response(HttpStatus.BAD_GATEWAY, exception.getMessage());
	}

	private ResponseEntity<ApiErrorResponse> response(HttpStatus status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), status.getReasonPhrase(), message));
	}
}
