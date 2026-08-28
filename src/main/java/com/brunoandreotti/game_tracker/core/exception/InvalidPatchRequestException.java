package com.brunoandreotti.game_tracker.core.exception;

public class InvalidPatchRequestException extends RuntimeException {

	public InvalidPatchRequestException() {
		super("PATCH must include status and/or rating");
	}

}
