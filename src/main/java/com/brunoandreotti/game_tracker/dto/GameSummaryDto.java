package com.brunoandreotti.game_tracker.dto;

public record GameSummaryDto(
		long rawgId,
		String name,
		Integer year,
		String coverUrl) {
}
