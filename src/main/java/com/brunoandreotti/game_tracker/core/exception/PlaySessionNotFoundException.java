package com.brunoandreotti.game_tracker.core.exception;

public class PlaySessionNotFoundException extends RuntimeException {

	public PlaySessionNotFoundException(long trackedGameId, long sessionId) {
		super("Play session not found: " + sessionId + " for tracked game: " + trackedGameId);
	}

}
