package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.GameSummary;

public record GameSearchResponse(long rawgId, String name, Integer year, String coverUrl) {

	public static GameSearchResponse from(GameSummary summary) {
		return new GameSearchResponse(summary.rawgId(), summary.name(), summary.year(), summary.coverUrl());
	}

}
