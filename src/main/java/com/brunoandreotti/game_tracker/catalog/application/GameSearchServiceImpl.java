package com.brunoandreotti.game_tracker.catalog.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSearchServiceImpl implements GameSearchService {

	private final GameCatalogPort gameCatalogPort;

	@Override
	public List<GameSummaryDto> search(String query) {
		return gameCatalogPort.search(query);
	}

}
