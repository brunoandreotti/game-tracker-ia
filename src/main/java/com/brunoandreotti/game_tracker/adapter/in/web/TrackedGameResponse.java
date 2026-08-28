package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.PlayStatus;
import com.brunoandreotti.game_tracker.core.model.TrackedGameModel;

public record TrackedGameResponse(
		long id,
		long rawgId,
		String name,
		Integer year,
		String coverUrl,
		PlayStatus status,
		Integer rating,
		int totalMinutes) {

	public static TrackedGameResponse from(TrackedGameModel model) {
		return new TrackedGameResponse(
				model.id(),
				model.rawgId(),
				model.name(),
				model.year(),
				model.coverUrl(),
				model.status(),
				model.rating(),
				model.totalMinutes());
	}

}
