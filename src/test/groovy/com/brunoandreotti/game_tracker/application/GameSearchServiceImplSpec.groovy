package com.brunoandreotti.game_tracker.application

import com.brunoandreotti.game_tracker.core.port.out.GameCatalogPort
import com.brunoandreotti.game_tracker.core.model.GameSummary
import spock.lang.Specification
import spock.lang.Subject

class GameSearchServiceImplSpec extends Specification {

	GameCatalogPort gameCatalogPort = Mock()

	@Subject
	GameSearchServiceImpl service = new GameSearchServiceImpl(gameCatalogPort)

	def "Given the catalog port returns a Zelda summary, When the service searches for zelda, Then the summaries are returned unchanged"() {
		given: "the catalog port will return a Zelda summary"
		def summaries = [new GameSummary(123L, "Zelda", 2017, "https://cover")]

		when: "the service searches for zelda"
		def results = service.search("zelda", false)

		then: "the summaries from the catalog are returned unchanged"
		1 * gameCatalogPort.search("zelda", false) >> summaries
		results == summaries
	}

	def "Given the catalog has no matches, When the service searches for nothing, Then an empty list is returned"() {
		when: "the service searches for a query with no matches"
		def results = service.search("nothing", false)

		then: "an empty list is returned"
		1 * gameCatalogPort.search("nothing", false) >> []
		results.isEmpty()
	}

	def "Given exact search is requested, When the service searches for Lies Of P, Then the catalog is called with exact true"() {
		given: "the catalog port will return a Lies Of P summary"
		def summaries = [new GameSummary(605674L, "Lies Of P", 2023, "https://cover")]

		when: "the service searches for Lies Of P with exact true"
		def results = service.search("Lies Of P", true)

		then: "the catalog is called with exact true"
		1 * gameCatalogPort.search("Lies Of P", true) >> summaries
		results == summaries
	}

}
