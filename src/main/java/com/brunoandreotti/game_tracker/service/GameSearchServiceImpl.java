package com.brunoandreotti.game_tracker.service;

import java.util.List;

import com.brunoandreotti.game_tracker.client.GameCatalogPort;
import com.brunoandreotti.game_tracker.dto.GameSummaryDto;
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
