package com.brunoandreotti.game_tracker.config;

import com.brunoandreotti.game_tracker.core.exception.CatalogUnavailableException;
import com.brunoandreotti.game_tracker.adapter.in.web.ApiErrorResponse;
import com.brunoandreotti.game_tracker.core.exception.GameNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
			MissingServletRequestParameterException.class
	})
	public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception exception) {
		return response(HttpStatus.BAD_REQUEST, exception.getMessage());
	}

	@ExceptionHandler(GameNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(GameNotFoundException exception) {
		return response(HttpStatus.NOT_FOUND, exception.getMessage());
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
