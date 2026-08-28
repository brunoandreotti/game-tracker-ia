package com.brunoandreotti.game_tracker.core.model;

public record TrackedGameModel(
		Long id,
		long rawgId,
		String name,
		Integer year,
		String coverUrl,
		PlayStatus status,
		Integer rating,
		int totalMinutes) {

	public static TrackedGameModel from(TrackedGame trackedGame, int totalMinutes) {
		return new TrackedGameModel(
				trackedGame.id(),
				trackedGame.rawgId(),
				trackedGame.name(),
				trackedGame.year(),
				trackedGame.coverUrl(),
				trackedGame.status(),
				trackedGame.rating(),
				totalMinutes);
	}

}
