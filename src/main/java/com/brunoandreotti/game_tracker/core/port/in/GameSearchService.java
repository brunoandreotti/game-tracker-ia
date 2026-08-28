package com.brunoandreotti.game_tracker.core.port.in;

import java.util.List;

import com.brunoandreotti.game_tracker.core.model.GameSummary;

public interface GameSearchService {

	List<GameSummary> search(String query, boolean exact);

}
