package com.brunoandreotti.game_tracker.application;

import java.util.List;

import com.brunoandreotti.game_tracker.core.model.GameSummary;
import com.brunoandreotti.game_tracker.core.port.in.GameSearchService;
import com.brunoandreotti.game_tracker.core.port.out.GameCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameSearchServiceImpl implements GameSearchService {

	private final GameCatalogPort gameCatalogPort;

	@Override
	public List<GameSummary> search(String query) {
		return gameCatalogPort.search(query);
	}

}
