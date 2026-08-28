package com.brunoandreotti.game_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.brunoandreotti.game_tracker.entity.TrackedGameEntity;
import com.brunoandreotti.game_tracker.exception.DuplicateRawgIdException;
import com.brunoandreotti.game_tracker.model.TrackedGame;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaTrackedGameRepository implements TrackedGameRepository {

	private final TrackedGameSpringDataRepository trackedGameSpringDataRepository;

	@Override
	@Transactional
	public TrackedGame save(TrackedGame trackedGame) {
		if (trackedGame.id() == null && trackedGameSpringDataRepository.existsByRawgId(trackedGame.rawgId())) {
			throw new DuplicateRawgIdException(trackedGame.rawgId());
		}

		TrackedGameEntity entity = trackedGame.id() == null
				? new TrackedGameEntity()
				: trackedGameSpringDataRepository.findById(trackedGame.id()).orElseGet(TrackedGameEntity::new);

		mapToEntity(trackedGame, entity);
		return mapToDomain(trackedGameSpringDataRepository.save(entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<TrackedGame> findById(long id) {
		return trackedGameSpringDataRepository.findById(id).map(this::mapToDomain);
	}

	@Override
	@Transactional(readOnly = true)
	public List<TrackedGame> findAllOrderByIdAsc() {
		return trackedGameSpringDataRepository.findAllByOrderByIdAsc().stream().map(this::mapToDomain).toList();
	}

	@Override
	@Transactional(readOnly = true)
	public boolean existsByRawgId(long rawgId) {
		return trackedGameSpringDataRepository.existsByRawgId(rawgId);
	}

	@Override
	@Transactional
	public void deleteById(long id) {
		trackedGameSpringDataRepository.deleteById(id);
	}

	private TrackedGame mapToDomain(TrackedGameEntity entity) {
		return new TrackedGame(
				entity.getId(),
				entity.getRawgId(),
				entity.getName(),
				entity.getYear(),
				entity.getCoverUrl(),
				entity.getStatus(),
				entity.getRating());
	}

	private void mapToEntity(TrackedGame trackedGame, TrackedGameEntity entity) {
		entity.setRawgId(trackedGame.rawgId());
		entity.setName(trackedGame.name());
		entity.setYear(trackedGame.year());
		entity.setCoverUrl(trackedGame.coverUrl());
		entity.setStatus(trackedGame.status());
		entity.setRating(trackedGame.rating());
	}

}
