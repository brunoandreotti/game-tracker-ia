package com.brunoandreotti.game_tracker.catalog.adapter.web

import com.brunoandreotti.game_tracker.support.WireMockRawgSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.get as wireMockGet
import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.serverError
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
@ContextConfiguration(initializers = WireMockRawgSupport.Initializer)
class GameSearchControllerSpec extends Specification {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")

	@Autowired
	MockMvc mockMvc

	@Autowired
	JdbcTemplate jdbcTemplate

	def setup() {
		WireMockRawgSupport.WIRE_MOCK.resetAll()
	}

	def "GET /games/search returns mapped games from RAWG"() {
		given:
		WireMockRawgSupport.WIRE_MOCK.stubFor(wireMockGet(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		expect:
		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isOk())
				.andExpect(jsonPath('$[0].rawgId').value(123))
				.andExpect(jsonPath('$[0].name').value("Zelda"))
				.andExpect(jsonPath('$[0].year').value(2017))
				.andExpect(jsonPath('$[0].coverUrl').value("https://cover"))
	}

	def "GET /games/search does not persist tracked games"() {
		given:
		WireMockRawgSupport.WIRE_MOCK.stubFor(wireMockGet(urlPathEqualTo("/games"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"Zelda","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when:
		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isOk())

		then:
		jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tracked_game", Integer) == 0
	}

	def "GET /games/search rejects blank q with 400"() {
		expect:
		mockMvc.perform(get("/games/search").param("q", "   "))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath('$.status').value(400))
				.andExpect(jsonPath('$.error').value("Bad Request"))
				.andExpect(jsonPath('$.message').exists())
	}

	def "GET /games/search returns 502 when RAWG is unavailable"() {
		given:
		WireMockRawgSupport.WIRE_MOCK.stubFor(wireMockGet(urlPathEqualTo("/games"))
				.willReturn(serverError()))

		expect:
		mockMvc.perform(get("/games/search").param("q", "zelda"))
				.andExpect(status().isBadGateway())
				.andExpect(jsonPath('$.status').value(502))
				.andExpect(jsonPath('$.error').value("Bad Gateway"))
				.andExpect(jsonPath('$.message').exists())
	}

	def "GET /games/search returns empty array when RAWG has no matches"() {
		given:
		WireMockRawgSupport.WIRE_MOCK.stubFor(wireMockGet(urlPathEqualTo("/games"))
				.willReturn(okJson('''{"count":0,"results":[]}''')))

		expect:
		mockMvc.perform(get("/games/search").param("q", "nothing"))
				.andExpect(status().isOk())
				.andExpect(jsonPath('$').isArray())
				.andExpect(jsonPath('$').isEmpty())
	}
}
