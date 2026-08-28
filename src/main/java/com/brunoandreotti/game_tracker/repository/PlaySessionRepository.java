package com.brunoandreotti.game_tracker.repository;

import java.util.List;
import java.util.Optional;

import com.brunoandreotti.game_tracker.model.PlaySession;

public interface PlaySessionRepository {

	PlaySession save(PlaySession playSession);

	List<PlaySession> listByTrackedGameId(long trackedGameId);

	Optional<PlaySession> findByIdAndTrackedGameId(long sessionId, long trackedGameId);

	void delete(PlaySession playSession);

	int sumDurationByTrackedGameId(long trackedGameId);

}
