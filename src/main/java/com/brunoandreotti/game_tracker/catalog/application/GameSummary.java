package com.brunoandreotti.game_tracker.catalog.application;

public record GameSummary(
		long rawgId,
		String name,
		Integer year,
		String coverUrl) {
}
