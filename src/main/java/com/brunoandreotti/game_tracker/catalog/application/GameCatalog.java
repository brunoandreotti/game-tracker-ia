package com.brunoandreotti.game_tracker.catalog.application;

import java.util.List;

public interface GameCatalog {

	List<GameSummary> search(String query);

	GameSummary getByRawgId(long rawgId);
}
