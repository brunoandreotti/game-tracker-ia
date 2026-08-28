package com.brunoandreotti.game_tracker.exception;

public class TrackedGameNotFoundException extends RuntimeException {

	private final long trackedGameId;

	public TrackedGameNotFoundException(long trackedGameId) {
		super("Tracked game not found: " + trackedGameId);
		this.trackedGameId = trackedGameId;
	}

	public long getTrackedGameId() {
		return trackedGameId;
	}

}
