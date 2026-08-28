package com.brunoandreotti.game_tracker.application;

import java.util.List;

import org.springframework.stereotype.Service;

import com.brunoandreotti.game_tracker.core.exception.DuplicateRawgIdException;
import com.brunoandreotti.game_tracker.core.exception.InvalidPatchRequestException;
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException;
import com.brunoandreotti.game_tracker.core.model.PlayStatus;
import com.brunoandreotti.game_tracker.core.model.TrackedGame;
import com.brunoandreotti.game_tracker.core.model.TrackedGameModel;
import com.brunoandreotti.game_tracker.core.port.in.TrackedGameService;
import com.brunoandreotti.game_tracker.core.port.out.GameCatalogPort;
import com.brunoandreotti.game_tracker.core.port.out.PlaySessionRepository;
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrackedGameServiceImpl implements TrackedGameService {

	private final GameCatalogPort gameCatalogPort;

	private final TrackedGameRepository trackedGameRepository;

	private final PlaySessionRepository playSessionRepository;

	@Override
	public TrackedGameModel add(long rawgId, PlayStatus status) {
		if (trackedGameRepository.existsByRawgId(rawgId)) {
			throw new DuplicateRawgIdException(rawgId);
		}

		var summary = gameCatalogPort.getByRawgId(rawgId);
		PlayStatus effectiveStatus = status != null ? status : PlayStatus.PLAYING;
		var trackedGame = new TrackedGame(
				null,
				summary.rawgId(),
				summary.name(),
				summary.year(),
				summary.coverUrl(),
				effectiveStatus,
				null);

		return toModel(trackedGameRepository.save(trackedGame));
	}

	@Override
	public List<TrackedGameModel> list() {
		return trackedGameRepository.findAllOrderByIdAsc().stream().map(this::toModel).toList();
	}

	@Override
	public TrackedGameModel get(long id) {
		return toModel(findTrackedGame(id));
	}

	@Override
	public TrackedGameModel patch(long id, PlayStatus status, Integer rating) {
		if (status == null && rating == null) {
			throw new InvalidPatchRequestException();
		}

		TrackedGame existing = findTrackedGame(id);
		PlayStatus newStatus = status != null ? status : existing.status();
		Integer newRating = rating != null ? rating : existing.rating();
		var updated = new TrackedGame(
				existing.id(),
				existing.rawgId(),
				existing.name(),
				existing.year(),
				existing.coverUrl(),
				newStatus,
				newRating);

		return toModel(trackedGameRepository.save(updated));
	}

	@Override
	public void delete(long id) {
		if (trackedGameRepository.findById(id).isEmpty()) {
			throw new TrackedGameNotFoundException(id);
		}
		trackedGameRepository.deleteById(id);
	}

	private TrackedGame findTrackedGame(long id) {
		return trackedGameRepository.findById(id).orElseThrow(() -> new TrackedGameNotFoundException(id));
	}

	private TrackedGameModel toModel(TrackedGame trackedGame) {
		int totalMinutes = playSessionRepository.sumDurationByTrackedGameId(trackedGame.id());
		return TrackedGameModel.from(trackedGame, totalMinutes);
	}

}
