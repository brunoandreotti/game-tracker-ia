package com.brunoandreotti.game_tracker.core.exception;

public class CatalogUnavailableException extends RuntimeException {

	public CatalogUnavailableException(String message) {
		super(message);
	}

	public CatalogUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
