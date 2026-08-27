package com.brunoandreotti.game_tracker.catalog.application;

public class GameNotFoundException extends RuntimeException {

	private final long rawgId;

	public GameNotFoundException(long rawgId) {
		super("Game not found on RAWG: " + rawgId);
		this.rawgId = rawgId;
	}

	public long getRawgId() {
		return rawgId;
	}
}
