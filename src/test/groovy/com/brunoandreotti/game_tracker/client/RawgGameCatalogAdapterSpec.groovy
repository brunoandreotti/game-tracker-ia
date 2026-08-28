package com.brunoandreotti.game_tracker.client

import com.brunoandreotti.game_tracker.exception.CatalogUnavailableException
import com.brunoandreotti.game_tracker.client.GameCatalogPort
import com.brunoandreotti.game_tracker.exception.GameNotFoundException
import com.brunoandreotti.game_tracker.config.RawgWireMockIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired

import static com.github.tomakehurst.wiremock.client.WireMock.*

class RawgGameCatalogAdapterSpec extends RawgWireMockIntegrationSpec {

	@Autowired
	GameCatalogPort gameCatalogPort

	def "Given RAWG returns a Zelda result with year and cover, When the catalog is searched for zelda, Then the result is mapped to a game summary"() {
		given: "RAWG returns a Zelda result with year and cover"
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when: "the catalog is searched for zelda"
		def results = gameCatalogPort.search("zelda")

		then: "the result is mapped to a game summary with rawgId, name, year, and coverUrl"
		results.size() == 1
		results[0].rawgId() == 123L
		results[0].name() == "Zelda"
		results[0].year() == 2017
		results[0].coverUrl() == "https://cover"
	}

	def "Given RAWG returns a game with null year and cover, When the catalog is searched for unknown, Then year and coverUrl are null"() {
		given: "RAWG returns a game with null year and cover"
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("unknown"))
				.willReturn(okJson('''{"count":1,"results":[{"id":456,"name":"No Cover","released":null,"background_image":null}]}''')))

		when: "the catalog is searched for unknown"
		def results = gameCatalogPort.search("unknown")

		then: "year and coverUrl are null"
		results[0].year() == null
		results[0].coverUrl() == null
	}

	def "Given RAWG returns 404 for game 999, When the catalog is asked for rawg id 999, Then GameNotFoundException is thrown"() {
		given: "RAWG returns 404 for game 999"
		wireMock().stubFor(get(urlPathEqualTo("/games/999"))
				.willReturn(notFound()))

		when: "the catalog is asked for rawg id 999"
		gameCatalogPort.getByRawgId(999L)

		then: "GameNotFoundException is thrown"
		thrown(GameNotFoundException)
	}

	def "Given RAWG returns a server error, When the catalog is searched for zelda, Then CatalogUnavailableException is thrown"() {
		given: "RAWG returns a server error"
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.willReturn(serverError()))

		when: "the catalog is searched for zelda"
		gameCatalogPort.search("zelda")

		then: "CatalogUnavailableException is thrown"
		thrown(CatalogUnavailableException)
	}

}
