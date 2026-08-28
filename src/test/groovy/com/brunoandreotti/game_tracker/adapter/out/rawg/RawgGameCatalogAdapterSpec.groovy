package com.brunoandreotti.game_tracker.adapter.out.rawg

import com.brunoandreotti.game_tracker.core.exception.CatalogUnavailableException
import com.brunoandreotti.game_tracker.core.port.out.GameCatalogPort
import com.brunoandreotti.game_tracker.core.exception.GameNotFoundException
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
				.withQueryParam("search_precise", equalTo("true"))
				.withQueryParam("search_exact", equalTo("false"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when: "the catalog is searched for zelda"
		def results = gameCatalogPort.search("zelda", false)

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
		def results = gameCatalogPort.search("unknown", false)

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
		gameCatalogPort.search("zelda", false)

		then: "CatalogUnavailableException is thrown"
		thrown(CatalogUnavailableException)
	}

	def "Given exact search is requested, When the catalog is searched for Lies Of P, Then RAWG is called with search_precise and search_exact"() {
		given: "RAWG returns an exact Lies Of P result"
		wireMock().stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("Lies Of P"))
				.withQueryParam("search_precise", equalTo("true"))
				.withQueryParam("search_exact", equalTo("true"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson('''{"count":1,"results":[{"id":605674,"name":"Lies Of P","released":"2023-09-19","background_image":"https://cover"}]}''')))

		when: "the catalog is searched for Lies Of P with exact true"
		def results = gameCatalogPort.search("Lies Of P", true)

		then: "the result is mapped to a game summary"
		results.size() == 1
		results[0].rawgId() == 605674L
		results[0].name() == "Lies Of P"
	}

}
