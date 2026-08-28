package com.brunoandreotti.game_tracker.client;

import java.util.List;

import com.brunoandreotti.game_tracker.dto.GameSummaryDto;

public interface GameCatalogPort {

	List<GameSummaryDto> search(String query);

	GameSummaryDto getByRawgId(long rawgId);

}
