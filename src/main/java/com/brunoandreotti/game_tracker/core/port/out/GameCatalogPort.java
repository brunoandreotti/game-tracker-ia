package com.brunoandreotti.game_tracker.core.port.out;

import java.util.List;

import com.brunoandreotti.game_tracker.core.model.GameSummary;

public interface GameCatalogPort {

	List<GameSummary> search(String query, boolean exact);

	GameSummary getByRawgId(long rawgId);

}
