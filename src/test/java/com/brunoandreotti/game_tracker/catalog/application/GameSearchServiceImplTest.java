package com.brunoandreotti.game_tracker.catalog.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GameSearchServiceImplTest {

	@Mock
	GameCatalogPort gameCatalogPort;

	@InjectMocks
	GameSearchServiceImpl service;

	@Test
	void returnsSummariesFromCatalogPort() {
		var summaries = List.of(new GameSummaryDto(123L, "Zelda", 2017, "https://cover"));
		when(gameCatalogPort.search("zelda")).thenReturn(summaries);

		var results = service.search("zelda");

		assertEquals(summaries, results);
		verify(gameCatalogPort).search("zelda");
	}

	@Test
	void returnsEmptyListWhenCatalogHasNoMatches() {
		when(gameCatalogPort.search("nothing")).thenReturn(List.of());

		var results = service.search("nothing");

		assertTrue(results.isEmpty());
		verify(gameCatalogPort).search("nothing");
	}

}
