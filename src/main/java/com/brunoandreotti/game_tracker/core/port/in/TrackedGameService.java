package com.brunoandreotti.game_tracker.core.port.in;

import java.util.List;

import com.brunoandreotti.game_tracker.core.model.PlayStatus;
import com.brunoandreotti.game_tracker.core.model.TrackedGameModel;

public interface TrackedGameService {

	TrackedGameModel add(long rawgId, PlayStatus status);

	List<TrackedGameModel> list();

	TrackedGameModel get(long id);

	TrackedGameModel patch(long id, PlayStatus status, Integer rating);

	void delete(long id);

}
