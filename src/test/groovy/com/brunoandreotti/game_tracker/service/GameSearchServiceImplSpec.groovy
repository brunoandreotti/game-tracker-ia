package com.brunoandreotti.game_tracker.service

import com.brunoandreotti.game_tracker.client.GameCatalogPort
import com.brunoandreotti.game_tracker.dto.GameSummaryDto
import spock.lang.Specification
import spock.lang.Subject

class GameSearchServiceImplSpec extends Specification {

	GameCatalogPort gameCatalogPort = Mock()

	@Subject
	GameSearchServiceImpl service = new GameSearchServiceImpl(gameCatalogPort)

	def "Given the catalog port returns a Zelda summary, When the service searches for zelda, Then the summaries are returned unchanged"() {
		given: "the catalog port will return a Zelda summary"
		def summaries = [new GameSummaryDto(123L, "Zelda", 2017, "https://cover")]

		when: "the service searches for zelda"
		def results = service.search("zelda")

		then: "the summaries from the catalog are returned unchanged"
		1 * gameCatalogPort.search("zelda") >> summaries
		results == summaries
	}

	def "Given the catalog has no matches, When the service searches for nothing, Then an empty list is returned"() {
		when: "the service searches for a query with no matches"
		def results = service.search("nothing")

		then: "an empty list is returned"
		1 * gameCatalogPort.search("nothing") >> []
		results.isEmpty()
	}

}
