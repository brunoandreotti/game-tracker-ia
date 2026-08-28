package com.brunoandreotti.game_tracker.adapter.in.web

import com.brunoandreotti.game_tracker.config.RawgMockMvcIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class SessionControllerSpec extends RawgMockMvcIntegrationSpec {

	@Autowired
	MockMvc mockMvc

	@Autowired
	JdbcTemplate jdbcTemplate

	def setup() {
		jdbcTemplate.execute("DELETE FROM play_session")
		jdbcTemplate.execute("DELETE FROM tracked_game")
	}

	def "Given tracked game 1 exists, When POST /tracked-games/1/sessions sets duration 90, Then the response is 201"() {
		given: "tracked game 1 exists"
		def trackedGameId = createTrackedGame(123L)

		when: "POST creates a 90 minute session"
		def result = mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90,"playedAt":"2026-08-27"}'))

		then: "the response is 201 with session fields"
		result.andExpect(status().isCreated())
				.andExpect(jsonPath('$.id').exists())
				.andExpect(jsonPath('$.durationMinutes').value(90))
				.andExpect(jsonPath('$.playedAt').value("2026-08-27"))
	}

	def "Given tracked game 1 exists, When POST /tracked-games/1/sessions omits playedAt, Then playedAt defaults to today"() {
		given: "tracked game 1 exists"
		def trackedGameId = createTrackedGame(123L)
		def today = java.time.LocalDate.now().toString()

		when: "POST omits playedAt"
		def result = mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":60}'))

		then: "playedAt defaults to today"
		result.andExpect(status().isCreated())
				.andExpect(jsonPath('$.playedAt').value(today))
	}

	def "Given tracked game 1 has two sessions, When GET /tracked-games/1/sessions is called, Then sessions are returned newest first"() {
		given: "tracked game 1 has two sessions"
		def trackedGameId = createTrackedGame(123L)
		mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":60,"playedAt":"2026-08-26"}'))
		mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90,"playedAt":"2026-08-27"}'))

		when: "GET /tracked-games/{id}/sessions is called"
		def result = mockMvc.perform(get("/tracked-games/${trackedGameId}/sessions"))

		then: "sessions are returned newest first"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$.length()').value(2))
				.andExpect(jsonPath('$[0].durationMinutes').value(90))
				.andExpect(jsonPath('$[1].durationMinutes').value(60))
	}

	def "Given tracked game 1 has a session, When DELETE /tracked-games/1/sessions/{sessionId} is called, Then the response is 204"() {
		given: "tracked game 1 has a session"
		def trackedGameId = createTrackedGame(123L)
		def create = mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90,"playedAt":"2026-08-27"}'))
				.andReturn()
		def sessionId = extractLongField(create.response.contentAsString, 'id')

		when: "DELETE is called"
		def result = mockMvc.perform(delete("/tracked-games/${trackedGameId}/sessions/${sessionId}"))

		then: "the response is 204"
		result.andExpect(status().isNoContent())
		mockMvc.perform(get("/tracked-games/${trackedGameId}/sessions")).andExpect(jsonPath('$.length()').value(0))
	}

	def "Given tracked game 1 exists, When POST /tracked-games/1/sessions sets duration 0, Then the response is 400"() {
		given: "tracked game 1 exists"
		def trackedGameId = createTrackedGame(123L)

		when: "POST sets duration 0"
		def result = mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":0}'))

		then: "the response is 400"
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath('$.status').value(400))
	}

	def "Given tracked game 99 does not exist, When POST /tracked-games/99/sessions is called, Then the response is 404"() {
		when: "POST /tracked-games/99/sessions is called"
		def result = mockMvc.perform(post("/tracked-games/99/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90}'))

		then: "the response is 404"
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath('$.status').value(404))
	}

	def "Given session 1 belongs to tracked game 2, When DELETE /tracked-games/1/sessions/1 is called, Then the response is 404"() {
		given: "session 1 belongs to tracked game 2"
		def trackedGameId = createTrackedGame(123L)
		def create = mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90,"playedAt":"2026-08-27"}'))
				.andReturn()
		def sessionId = extractLongField(create.response.contentAsString, 'id')

		when: "DELETE uses the wrong tracked game id"
		def result = mockMvc.perform(delete("/tracked-games/999/sessions/${sessionId}"))

		then: "the response is 404"
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath('$.status').value(404))
	}

	def "Given RAWG returns Zelda, When the v1 demo flow runs, Then totalMinutes is 150 and PATCH sets rating 9 and COMPLETED"() {
		given: "RAWG returns Zelda"
		stubRawgGame(123L, "The Legend of Zelda: Breath of the Wild", "2017-03-03", "https://cover")
		wireMock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/games"))
				.withQueryParam("search", equalTo("zelda"))
				.willReturn(okJson('''{"count":1,"results":[{"id":123,"name":"The Legend of Zelda: Breath of the Wild","released":"2017-03-03","background_image":"https://cover"}]}''')))

		when: "the v1 demo flow runs"
		mockMvc.perform(get("/games/search").param("q", "zelda")).andExpect(status().isOk())
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andExpect(status().isCreated())
				.andExpect(jsonPath('$.status').value("PLAYING"))
				.andReturn()
		def trackedGameId = extractLongField(create.response.contentAsString, 'id')
		mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":90,"playedAt":"2026-08-27"}'))
		mockMvc.perform(post("/tracked-games/${trackedGameId}/sessions")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"durationMinutes":60,"playedAt":"2026-08-26"}'))
		def afterSessions = mockMvc.perform(get("/tracked-games/${trackedGameId}"))
		def patch = mockMvc.perform(patch("/tracked-games/${trackedGameId}")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rating":9,"status":"COMPLETED"}'))

		then: "totalMinutes is 150 and PATCH sets rating 9 and COMPLETED"
		afterSessions.andExpect(status().isOk())
				.andExpect(jsonPath('$.totalMinutes').value(150))
		patch.andExpect(status().isOk())
				.andExpect(jsonPath('$.rating').value(9))
				.andExpect(jsonPath('$.status').value("COMPLETED"))
				.andExpect(jsonPath('$.totalMinutes').value(150))
	}

	private long createTrackedGame(long rawgId) {
		stubRawgGame(rawgId, "Game ${rawgId}", "2020-01-01", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""{"rawgId":${rawgId}}"""))
				.andExpect(status().isCreated())
				.andReturn()
		extractLongField(create.response.contentAsString, 'id')
	}

	private long extractLongField(String json, String field) {
		def matcher = (json =~ /"${field}"\s*:\s*(\d+)/)
		matcher.find()
		matcher.group(1) as long
	}

	private void stubRawgGame(long rawgId, String name, String released, String cover) {
		wireMock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/games/${rawgId}"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson("""{"id":${rawgId},"name":"${name}","released":"${released}","background_image":"${cover}"}""")))
	}

}
