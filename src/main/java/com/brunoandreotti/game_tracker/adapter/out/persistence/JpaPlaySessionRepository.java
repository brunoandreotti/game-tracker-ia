package com.brunoandreotti.game_tracker.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brunoandreotti.game_tracker.adapter.out.persistence.PlaySessionEntity;
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException;
import com.brunoandreotti.game_tracker.core.model.PlaySession;
import com.brunoandreotti.game_tracker.core.port.out.PlaySessionRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaPlaySessionRepository implements PlaySessionRepository {

	private final PlaySessionSpringDataRepository playSessionSpringDataRepository;

	private final TrackedGameSpringDataRepository trackedGameSpringDataRepository;

	@Override
	@Transactional
	public PlaySession save(PlaySession playSession) {
		var trackedGame = trackedGameSpringDataRepository.findById(playSession.trackedGameId())
				.orElseThrow(() -> new TrackedGameNotFoundException(playSession.trackedGameId()));

		PlaySessionEntity entity = playSession.id() == null
				? new PlaySessionEntity()
				: playSessionSpringDataRepository.findByIdAndTrackedGameId(playSession.id(), playSession.trackedGameId())
						.orElseGet(PlaySessionEntity::new);

		entity.setTrackedGame(trackedGame);
		entity.setDurationMinutes(playSession.durationMinutes());
		entity.setPlayedAt(playSession.playedAt());
		return mapToDomain(playSessionSpringDataRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public List<PlaySession> listByTrackedGameId(long trackedGameId) {
		return playSessionSpringDataRepository.findByTrackedGame_IdOrderByPlayedAtDescIdDesc(trackedGameId)
				.stream()
				.map(this::mapToDomain)
				.toList();
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<PlaySession> findByIdAndTrackedGameId(long sessionId, long trackedGameId) {
		return playSessionSpringDataRepository.findByIdAndTrackedGameId(sessionId, trackedGameId).map(this::mapToDomain);
	}

	@Override
	@Transactional
	public void delete(PlaySession playSession) {
		playSessionSpringDataRepository.findByIdAndTrackedGameId(playSession.id(), playSession.trackedGameId())
				.ifPresent(playSessionSpringDataRepository::delete);
	}

	@Override
	@Transactional(readOnly = true)
	public int sumDurationByTrackedGameId(long trackedGameId) {
		return playSessionSpringDataRepository.sumDurationByTrackedGameId(trackedGameId);
	}

	private PlaySession mapToDomain(PlaySessionEntity entity) {
		return new PlaySession(
				entity.getId(),
				entity.getTrackedGame().getId(),
				entity.getDurationMinutes(),
				entity.getPlayedAt());
	}

}
