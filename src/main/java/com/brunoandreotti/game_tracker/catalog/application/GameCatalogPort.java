package com.brunoandreotti.game_tracker.catalog.application;

import java.util.List;

public interface GameCatalogPort {

	List<GameSummaryDto> search(String query);

	GameSummaryDto getByRawgId(long rawgId);

}
