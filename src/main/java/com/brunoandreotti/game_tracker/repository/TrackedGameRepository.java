package com.brunoandreotti.game_tracker.repository;

import java.util.List;
import java.util.Optional;

import com.brunoandreotti.game_tracker.model.TrackedGame;

public interface TrackedGameRepository {

	TrackedGame save(TrackedGame trackedGame);

	Optional<TrackedGame> findById(long id);

	List<TrackedGame> findAllOrderByIdAsc();

	boolean existsByRawgId(long rawgId);

	void deleteById(long id);

}
