package com.brunoandreotti.game_tracker.dto;

import com.brunoandreotti.game_tracker.dto.GameSummaryDto;

public record GameSearchResponse(long rawgId, String name, Integer year, String coverUrl) {

	public static GameSearchResponse from(GameSummaryDto summary) {
		return new GameSearchResponse(summary.rawgId(), summary.name(), summary.year(), summary.coverUrl());
	}

}
