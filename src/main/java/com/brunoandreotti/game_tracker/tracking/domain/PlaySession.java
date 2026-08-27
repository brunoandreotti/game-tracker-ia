package com.brunoandreotti.game_tracker.tracking.domain;

import java.time.LocalDate;

public record PlaySession(
		Long id,
		long trackedGameId,
		int durationMinutes,
		LocalDate playedAt) {
}
