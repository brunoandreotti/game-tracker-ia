package com.brunoandreotti.game_tracker.catalog.adapter.http;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.notFound;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.brunoandreotti.game_tracker.catalog.application.CatalogUnavailableException;
import com.brunoandreotti.game_tracker.catalog.application.GameCatalogPort;
import com.brunoandreotti.game_tracker.catalog.application.GameNotFoundException;
import com.brunoandreotti.game_tracker.config.AbstractRawgWireMockIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RawgGameCatalogAdapterTest extends AbstractRawgWireMockIntegrationTest {

	@Autowired
	GameCatalogPort gameCatalogPort;

	@Test
	void searchMapsRawgResultsToGameSummaries() {
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson("""
						{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}
						""")));

		var results = gameCatalogPort.search("zelda");

		assertEquals(1, results.size());
		assertEquals(123L, results.getFirst().rawgId());
		assertEquals("Zelda", results.getFirst().name());
		assertEquals(2017, results.getFirst().year());
		assertEquals("https://cover", results.getFirst().coverUrl());
	}

	@Test
	void searchAllowsNullYearAndCoverFromRawg() {
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("unknown"))
				.willReturn(okJson("""
						{"count":1,"results":[{"id":456,"name":"No Cover","released":null,"background_image":null}]}
						""")));

		var results = gameCatalogPort.search("unknown");

		assertNull(results.getFirst().year());
		assertNull(results.getFirst().coverUrl());
	}

	@Test
	void getByRawgIdThrowsWhenRawgReturns404() {
		wireMock().stubFor(get(urlPathEqualTo("/games/999"))
				.willReturn(notFound()));

		assertThrows(GameNotFoundException.class, () -> gameCatalogPort.getByRawgId(999L));
	}

	@Test
	void searchThrowsWhenRawgReturns5xx() {
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.willReturn(serverError()));

		assertThrows(CatalogUnavailableException.class, () -> gameCatalogPort.search("zelda"));
	}

}
