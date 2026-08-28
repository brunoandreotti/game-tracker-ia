package com.brunoandreotti.game_tracker.core.port.out;

import java.util.List;

import com.brunoandreotti.game_tracker.core.model.GameSummary;

public interface GameCatalogPort {

	List<GameSummary> search(String query);

	GameSummary getByRawgId(long rawgId);

}
