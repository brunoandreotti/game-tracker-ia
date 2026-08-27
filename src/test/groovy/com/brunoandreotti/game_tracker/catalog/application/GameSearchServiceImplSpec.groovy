package com.brunoandreotti.game_tracker.catalog.application

import com.brunoandreotti.game_tracker.catalog.application.GameSearchServiceImpl
import spock.lang.Specification
import spock.lang.Subject

class GameSearchServiceImplSpec extends Specification {

	GameCatalog gameCatalog = Mock()

	@Subject
	GameSearchServiceImpl service = new GameSearchServiceImpl(gameCatalog)

	def "returns summaries from the catalog port"() {
		given:
		def summaries = [new GameSummary(123L, "Zelda", 2017, "https://cover")]

		when:
		def results = service.search("zelda")

		then:
		1 * gameCatalog.search("zelda") >> summaries
		results == summaries
	}

	def "returns empty list when the catalog has no matches"() {
		when:
		def results = service.search("nothing")

		then:
		1 * gameCatalog.search("nothing") >> []
		results.isEmpty()
	}
}
