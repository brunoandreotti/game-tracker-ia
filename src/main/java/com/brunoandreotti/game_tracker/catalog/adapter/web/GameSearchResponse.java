package com.brunoandreotti.game_tracker.catalog.adapter.web;

import com.brunoandreotti.game_tracker.catalog.application.GameSummary;

public record GameSearchResponse(long rawgId, String name, Integer year, String coverUrl) {

	public static GameSearchResponse from(GameSummary summary) {
		return new GameSearchResponse(summary.rawgId(), summary.name(), summary.year(), summary.coverUrl());
	}
}
