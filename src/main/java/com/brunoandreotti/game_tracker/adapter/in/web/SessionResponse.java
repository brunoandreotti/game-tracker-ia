package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.SessionModel;

public record SessionResponse(long id, int durationMinutes, String playedAt) {

	public static SessionResponse from(SessionModel model) {
		return new SessionResponse(model.id(), model.durationMinutes(), model.playedAt().toString());
	}

}
