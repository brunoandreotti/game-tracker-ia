package com.brunoandreotti.game_tracker.core.port.in;

import java.time.LocalDate;
import java.util.List;

import com.brunoandreotti.game_tracker.core.model.SessionModel;

public interface SessionService {

	SessionModel add(long trackedGameId, int durationMinutes, LocalDate playedAt);

	List<SessionModel> list(long trackedGameId);

	void delete(long trackedGameId, long sessionId);

}
