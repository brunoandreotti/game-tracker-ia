package com.brunoandreotti.game_tracker.catalog.adapter.web;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.brunoandreotti.game_tracker.config.AbstractRawgMockMvcIntegrationTest;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

class GameSearchControllerTest extends AbstractRawgMockMvcIntegrationTest {

	private final MockMvc mockMvc;
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	GameSearchControllerTest(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
		this.mockMvc = mockMvc;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Test
	void getGamesSearchReturnsMappedGamesFromRawg() throws Exception {
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.willReturn(okJson("""
						{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}
						""")));

		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].rawgId").value(123))
				.andExpect(jsonPath("$[0].name").value("Zelda"))
				.andExpect(jsonPath("$[0].year").value(2017))
				.andExpect(jsonPath("$[0].coverUrl").value("https://cover"));
	}

	@Test
	void getGamesSearchDoesNotPersistTrackedGames() throws Exception {
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(okJson("""
						{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}
						""")));

		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isOk());

		assertEquals(0, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tracked_game", Integer.class));
	}

	@Test
	void getGamesSearchRejectsBlankQueryWith400() throws Exception {
		mockMvc.perform(get("/games/search").param("q", "   "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void getGamesSearchReturns502WhenRawgIsUnavailable() throws Exception {
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(serverError()));

		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath("$.status").value(502))
				.andExpect(jsonPath("$.error").value("Bad Gateway"))
				.andExpect(jsonPath("$.message").exists());
	}

	@Test
	void getGamesSearchReturnsEmptyArrayWhenRawgHasNoMatches() throws Exception {
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(okJson("""
						{"count":0,"results":[]}
						""")));

		mockMvc.perform(get("/games/search").param("q", "nothing"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray())
				.andExpect(jsonPath("$").isEmpty());
	}

}
