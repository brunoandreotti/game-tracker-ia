package com.brunoandreotti.game_tracker.catalog.adapter.http

import com.brunoandreotti.game_tracker.catalog.application.CatalogUnavailableException
import com.brunoandreotti.game_tracker.catalog.application.GameCatalog
import com.brunoandreotti.game_tracker.catalog.application.GameNotFoundException
import com.brunoandreotti.game_tracker.catalog.application.GameSummary
import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Shared
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(initializers = RawgGameCatalogSpec.Initializer)
class RawgGameCatalogSpec extends Specification {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")

	@Shared
	static WireMockServer wireMock = new WireMockServer(0)

	static {
		wireMock.start()
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		void initialize(ConfigurableApplicationContext context) {
			TestPropertyValues.of(
					"spring.http.serviceclient.rawg.base-url=" + wireMock.baseUrl(),
					"rawg.api-key=test-key",
					"spring.http.serviceclient.rawg.connect-timeout=500ms",
					"spring.http.serviceclient.rawg.read-timeout=500ms"
			).applyTo(context.environment)
		}
	}

	def cleanupSpec() {
		wireMock.stop()
	}

	def setup() {
		wireMock.resetAll()
	}

	@Autowired
	GameCatalog gameCatalog

	def "search maps RAWG results to game summaries"() {
		given:
		wireMock.stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when:
		def results = gameCatalog.search("zelda")

		then:
		results.size() == 1
		results[0].rawgId() == 123L
		results[0].name() == "Zelda"
		results[0].year() == 2017
		results[0].coverUrl() == "https://cover"
	}

	def "search allows null year and cover from RAWG"() {
		given:
		wireMock.stubFor(get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("unknown"))
				.willReturn(okJson('''{"count":1,"results":[{"id":456,"name":"No Cover","released":null,"background_image":null}]}''')))

		when:
		def results = gameCatalog.search("unknown")

		then:
		results[0].year() == null
		results[0].coverUrl() == null
	}

	def "getByRawgId throws when RAWG returns 404"() {
		given:
		wireMock.stubFor(get(urlPathEqualTo("/games/999"))
				.willReturn(notFound()))

		when:
		gameCatalog.getByRawgId(999L)

		then:
		thrown(GameNotFoundException)
	}

	def "search throws when RAWG returns 5xx"() {
		given:
		wireMock.stubFor(get(urlPathEqualTo("/games"))
				.willReturn(serverError()))

		when:
		gameCatalog.search("zelda")

		then:
		thrown(CatalogUnavailableException)
	}
}
