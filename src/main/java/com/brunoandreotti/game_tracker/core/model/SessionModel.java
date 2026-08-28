package com.brunoandreotti.game_tracker.core.model;

import java.time.LocalDate;

public record SessionModel(long id, int durationMinutes, LocalDate playedAt) {

	public static SessionModel from(PlaySession playSession) {
		return new SessionModel(playSession.id(), playSession.durationMinutes(), playSession.playedAt());
	}

}
