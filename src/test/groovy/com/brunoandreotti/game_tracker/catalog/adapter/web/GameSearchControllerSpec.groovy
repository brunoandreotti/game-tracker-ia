package com.brunoandreotti.game_tracker.catalog.adapter.web

import com.brunoandreotti.game_tracker.config.RawgMockMvcIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc

import com.github.tomakehurst.wiremock.client.WireMock

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.serverError
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

class GameSearchControllerSpec extends RawgMockMvcIntegrationSpec {

	@Autowired
	MockMvc mockMvc

	@Autowired
	JdbcTemplate jdbcTemplate

	def "Given RAWG returns a Zelda result, When GET /games/search is called with q=zelda, Then the response is 200 with mapped fields"() {
		given: "RAWG returns a Zelda result"
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when: "GET /games/search is called with q=zelda"
		def result = mockMvc.perform(get("/games/search").param("q", "zelda"))

		then: "the response is 200 with rawgId, name, year, and coverUrl"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$[0].rawgId').value(123))
				.andExpect(jsonPath('$[0].name').value("Zelda"))
				.andExpect(jsonPath('$[0].year').value(2017))
				.andExpect(jsonPath('$[0].coverUrl').value("https://cover"))
	}

	def "Given RAWG returns a Zelda result, When GET /games/search is called with q=zelda, Then no tracked game is persisted"() {
		given: "RAWG returns a Zelda result"
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when: "GET /games/search is called with q=zelda"
		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isOk())

		then: "no tracked game is persisted"
		jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tracked_game", Integer) == 0
	}

	def "Given a blank search query, When GET /games/search is called, Then the response is 400 with status, error, and message"() {
		given: "a blank search query"
		def query = "   "

		when: "GET /games/search is called"
		def result = mockMvc.perform(get("/games/search").param("q", query))

		then: "the response is 400 with status, error, and message"
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath('$.status').value(400))
				.andExpect(jsonPath('$.error').value("Bad Request"))
				.andExpect(jsonPath('$.message').exists())
	}

	def "Given RAWG is unavailable, When GET /games/search is called with q=zelda, Then the response is 502 with status, error, and message"() {
		given: "RAWG returns a server error"
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(serverError()))

		when: "GET /games/search is called with q=zelda"
		def result = mockMvc.perform(get("/games/search").param("q", "zelda"))

		then: "the response is 502 with status, error, and message"
		result.andExpect(status().isBadGateway())
				.andExpect(jsonPath('$.status').value(502))
				.andExpect(jsonPath('$.error').value("Bad Gateway"))
				.andExpect(jsonPath('$.message').exists())
	}

	def "Given RAWG returns no matches, When GET /games/search is called with q=nothing, Then the response is 200 and an empty JSON array"() {
		given: "RAWG returns no matches"
		wireMock().stubFor(WireMock.get(urlPathEqualTo("/games"))
				.willReturn(okJson('''{"count":0,"results":[]}''')))

		when: "GET /games/search is called with q=nothing"
		def result = mockMvc.perform(get("/games/search").param("q", "nothing"))

		then: "the response is 200 and an empty JSON array"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$').isArray())
				.andExpect(jsonPath('$').isEmpty())
	}

}
