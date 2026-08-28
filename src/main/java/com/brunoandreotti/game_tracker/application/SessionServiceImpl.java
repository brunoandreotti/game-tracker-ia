package com.brunoandreotti.game_tracker.application;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.brunoandreotti.game_tracker.core.exception.InvalidDurationException;
import com.brunoandreotti.game_tracker.core.exception.PlaySessionNotFoundException;
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException;
import com.brunoandreotti.game_tracker.core.model.PlaySession;
import com.brunoandreotti.game_tracker.core.model.SessionModel;
import com.brunoandreotti.game_tracker.core.port.in.SessionService;
import com.brunoandreotti.game_tracker.core.port.out.PlaySessionRepository;
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {

	private final TrackedGameRepository trackedGameRepository;

	private final PlaySessionRepository playSessionRepository;

	private final Clock clock;

	@Override
	public SessionModel add(long trackedGameId, int durationMinutes, LocalDate playedAt) {
		if (durationMinutes <= 0) {
			throw new InvalidDurationException();
		}

		ensureTrackedGameExists(trackedGameId);
		LocalDate effectiveDate = playedAt != null ? playedAt : LocalDate.now(clock);
		var session = new PlaySession(null, trackedGameId, durationMinutes, effectiveDate);
		return SessionModel.from(playSessionRepository.save(session));
	}

	@Override
	public List<SessionModel> list(long trackedGameId) {
		ensureTrackedGameExists(trackedGameId);
		return playSessionRepository.listByTrackedGameId(trackedGameId).stream().map(SessionModel::from).toList();
	}

	@Override
	public void delete(long trackedGameId, long sessionId) {
		ensureTrackedGameExists(trackedGameId);
		PlaySession session = playSessionRepository.findByIdAndTrackedGameId(sessionId, trackedGameId)
				.orElseThrow(() -> new PlaySessionNotFoundException(trackedGameId, sessionId));
		playSessionRepository.delete(session);
	}

	private void ensureTrackedGameExists(long trackedGameId) {
		if (trackedGameRepository.findById(trackedGameId).isEmpty()) {
			throw new TrackedGameNotFoundException(trackedGameId);
		}
	}

}
