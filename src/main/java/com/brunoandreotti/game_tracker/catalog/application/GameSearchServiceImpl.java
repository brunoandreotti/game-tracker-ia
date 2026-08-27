package com.brunoandreotti.game_tracker.catalog.application;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSearchServiceImpl implements GameSearchService {

	private final GameCatalog gameCatalog;

	@Override
	public List<GameSummary> search(String query) {
		return gameCatalog.search(query);
	}
}
