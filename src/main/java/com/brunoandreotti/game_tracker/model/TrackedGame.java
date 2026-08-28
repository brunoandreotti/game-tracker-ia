package com.brunoandreotti.game_tracker.model;

public record TrackedGame(
		Long id,
		long rawgId,
		String name,
		Integer year,
		String coverUrl,
		PlayStatus status,
		Integer rating) {
}
