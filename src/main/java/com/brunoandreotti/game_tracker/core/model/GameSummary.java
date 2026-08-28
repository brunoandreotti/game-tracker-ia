package com.brunoandreotti.game_tracker.core.model;

public record GameSummary(
		long rawgId,
		String name,
		Integer year,
		String coverUrl) {
}
