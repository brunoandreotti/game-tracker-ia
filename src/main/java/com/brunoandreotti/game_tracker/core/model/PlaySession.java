package com.brunoandreotti.game_tracker.core.model;

import java.time.LocalDate;

public record PlaySession(
		Long id,
		long trackedGameId,
		int durationMinutes,
		LocalDate playedAt) {
}
