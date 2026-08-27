package com.brunoandreotti.game_tracker.catalog.application;

import java.util.List;

public interface GameSearchService {

	List<GameSummary> search(String query);
}
