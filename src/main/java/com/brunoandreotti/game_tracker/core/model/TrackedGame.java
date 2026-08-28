package com.brunoandreotti.game_tracker.core.model;

public record TrackedGame(
		Long id,
		long rawgId,
		String name,
		Integer year,
		String coverUrl,
		PlayStatus status,
		Integer rating) {
}
