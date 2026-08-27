package com.brunoandreotti.game_tracker.tracking.domain;

public class DuplicateRawgIdException extends RuntimeException {

	private final long rawgId;

	public DuplicateRawgIdException(long rawgId) {
		super("Tracked game already exists for RAWG id: " + rawgId);
		this.rawgId = rawgId;
	}

	public long getRawgId() {
		return rawgId;
	}

}
