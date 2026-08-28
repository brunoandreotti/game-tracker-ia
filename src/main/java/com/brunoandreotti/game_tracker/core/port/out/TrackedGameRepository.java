package com.brunoandreotti.game_tracker.core.port.out;

import java.util.List;
import java.util.Optional;

import com.brunoandreotti.game_tracker.core.model.TrackedGame;

public interface TrackedGameRepository {

	TrackedGame save(TrackedGame trackedGame);

	Optional<TrackedGame> findById(long id);

	List<TrackedGame> findAllOrderByIdAsc();

	boolean existsByRawgId(long rawgId);

	void deleteById(long id);

}
