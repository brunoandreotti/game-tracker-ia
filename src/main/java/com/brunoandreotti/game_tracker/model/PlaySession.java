package com.brunoandreotti.game_tracker.model;

import java.time.LocalDate;

public record PlaySession(
		Long id,
		long trackedGameId,
		int durationMinutes,
		LocalDate playedAt) {
}
