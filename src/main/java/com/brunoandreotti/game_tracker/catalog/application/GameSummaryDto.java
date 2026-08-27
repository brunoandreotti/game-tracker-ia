package com.brunoandreotti.game_tracker.catalog.application;

public record GameSummaryDto(
		long rawgId,
		String name,
		Integer year,
		String coverUrl) {
}
