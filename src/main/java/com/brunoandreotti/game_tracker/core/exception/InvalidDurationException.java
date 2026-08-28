package com.brunoandreotti.game_tracker.core.exception;

public class InvalidDurationException extends RuntimeException {

	public InvalidDurationException() {
		super("durationMinutes must be greater than 0");
	}

}
