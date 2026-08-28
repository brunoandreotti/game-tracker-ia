package com.brunoandreotti.game_tracker.service;

import java.util.List;

import com.brunoandreotti.game_tracker.dto.GameSummaryDto;

public interface GameSearchService {

	List<GameSummaryDto> search(String query);

}
