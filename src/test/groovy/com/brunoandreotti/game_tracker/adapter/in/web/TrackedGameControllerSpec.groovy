package com.brunoandreotti.game_tracker.adapter.in.web

import com.brunoandreotti.game_tracker.config.RawgMockMvcIntegrationSpec
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.web.servlet.MockMvc

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo
import static com.github.tomakehurst.wiremock.client.WireMock.notFound
import static com.github.tomakehurst.wiremock.client.WireMock.okJson
import static com.github.tomakehurst.wiremock.client.WireMock.serverError
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class TrackedGameControllerSpec extends RawgMockMvcIntegrationSpec {

	@Autowired
	MockMvc mockMvc

	@Autowired
	JdbcTemplate jdbcTemplate

	def setup() {
		jdbcTemplate.execute("DELETE FROM play_session")
		jdbcTemplate.execute("DELETE FROM tracked_game")
	}

	def "Given RAWG returns Zelda for rawgId 123, When POST /tracked-games omits status, Then the response is 201 with PLAYING and totalMinutes 0"() {
		given: "RAWG returns Zelda for rawgId 123"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")

		when: "POST /tracked-games omits status"
		def result = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))

		then: "the response is 201 with PLAYING and totalMinutes 0"
		result.andExpect(status().isCreated())
				.andExpect(jsonPath('$.id').exists())
				.andExpect(jsonPath('$.rawgId').value(123))
				.andExpect(jsonPath('$.name').value("Zelda"))
				.andExpect(jsonPath('$.year').value(2017))
				.andExpect(jsonPath('$.coverUrl').value("https://cover"))
				.andExpect(jsonPath('$.status').value("PLAYING"))
				.andExpect(jsonPath('$.rating').isEmpty())
				.andExpect(jsonPath('$.totalMinutes').value(0))
	}

	def "Given RAWG returns Zelda for rawgId 123, When POST /tracked-games sets WANT_TO_PLAY, Then that status is returned"() {
		given: "RAWG returns Zelda for rawgId 123"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")

		when: "POST /tracked-games sets WANT_TO_PLAY"
		def result = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123,"status":"WANT_TO_PLAY"}'))

		then: "that status is returned"
		result.andExpect(status().isCreated())
				.andExpect(jsonPath('$.status').value("WANT_TO_PLAY"))
	}

	def "Given rawgId 123 is already tracked, When POST /tracked-games is called again, Then the response is 409"() {
		given: "rawgId 123 is already tracked"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andExpect(status().isCreated())

		when: "POST /tracked-games is called again"
		def result = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))

		then: "the response is 409"
		result.andExpect(status().isConflict())
				.andExpect(jsonPath('$.status').value(409))
				.andExpect(jsonPath('$.error').value("Conflict"))
				.andExpect(jsonPath('$.message').exists())
	}

	def "Given RAWG returns 404 for rawgId 999, When POST /tracked-games is called, Then the response is 404"() {
		given: "RAWG returns 404 for rawgId 999"
		wireMock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/games/999")).willReturn(notFound()))

		when: "POST /tracked-games is called"
		def result = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":999}'))

		then: "the response is 404"
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath('$.status').value(404))
				.andExpect(jsonPath('$.message').exists())
	}

	def "Given RAWG is unavailable, When POST /tracked-games is called, Then the response is 502"() {
		given: "RAWG is unavailable"
		wireMock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/games/123")).willReturn(serverError()))

		when: "POST /tracked-games is called"
		def result = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))

		then: "the response is 502"
		result.andExpect(status().isBadGateway())
				.andExpect(jsonPath('$.status').value(502))
				.andExpect(jsonPath('$.message').exists())
	}

	def "Given two tracked games exist, When GET /tracked-games is called, Then both entries are returned ordered by id"() {
		given: "two tracked games exist"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		stubRawgGame(456L, "Mario", "2020-01-01", null)
		mockMvc.perform(post("/tracked-games").contentType(MediaType.APPLICATION_JSON).content('{"rawgId":123}'))
		mockMvc.perform(post("/tracked-games").contentType(MediaType.APPLICATION_JSON).content('{"rawgId":456}'))

		when: "GET /tracked-games is called"
		def result = mockMvc.perform(get("/tracked-games"))

		then: "both entries are returned ordered by id"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$.length()').value(2))
				.andExpect(jsonPath('$[0].rawgId').value(123))
				.andExpect(jsonPath('$[1].rawgId').value(456))
	}

	def "Given tracked game 1 exists, When GET /tracked-games/1 is called, Then the entry is returned"() {
		given: "tracked game 1 exists"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andExpect(status().isCreated())
				.andReturn()
		def id = extractLongField(create.response.contentAsString, 'id')

		when: "GET /tracked-games/{id} is called"
		def result = mockMvc.perform(get("/tracked-games/${id}"))

		then: "the entry is returned"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$.id').value(id))
				.andExpect(jsonPath('$.rawgId').value(123))
	}

	def "Given tracked game 99 does not exist, When GET /tracked-games/99 is called, Then the response is 404"() {
		when: "GET /tracked-games/99 is called"
		def result = mockMvc.perform(get("/tracked-games/99"))

		then: "the response is 404"
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath('$.status').value(404))
	}

	def "Given tracked game 1 exists, When PATCH /tracked-games/1 sets rating 9, Then rating is updated"() {
		given: "tracked game 1 exists"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andReturn()
		def id = extractLongField(create.response.contentAsString, 'id')

		when: "PATCH sets rating 9"
		def result = mockMvc.perform(patch("/tracked-games/${id}")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rating":9}'))

		then: "rating is updated"
		result.andExpect(status().isOk())
				.andExpect(jsonPath('$.rating').value(9))
				.andExpect(jsonPath('$.status').value("PLAYING"))
	}

	def "Given tracked game 1 exists, When PATCH /tracked-games/1 sends an empty body, Then the response is 400"() {
		given: "tracked game 1 exists"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andReturn()
		def id = extractLongField(create.response.contentAsString, 'id')

		when: "PATCH sends an empty body"
		def result = mockMvc.perform(patch("/tracked-games/${id}")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{}'))

		then: "the response is 400"
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath('$.status').value(400))
	}

	def "Given tracked game 1 exists, When PATCH /tracked-games/1 sets rating 11, Then the response is 400"() {
		given: "tracked game 1 exists"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andReturn()
		def id = extractLongField(create.response.contentAsString, 'id')

		when: "PATCH sets rating 11"
		def result = mockMvc.perform(patch("/tracked-games/${id}")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rating":11}'))

		then: "the response is 400"
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath('$.status').value(400))
	}

	def "Given tracked game 1 exists, When DELETE /tracked-games/1 is called, Then the response is 204"() {
		given: "tracked game 1 exists"
		stubRawgGame(123L, "Zelda", "2017-03-03", "https://cover")
		def create = mockMvc.perform(post("/tracked-games")
				.contentType(MediaType.APPLICATION_JSON)
				.content('{"rawgId":123}'))
				.andReturn()
		def id = extractLongField(create.response.contentAsString, 'id')

		when: "DELETE /tracked-games/{id} is called"
		def result = mockMvc.perform(delete("/tracked-games/${id}"))

		then: "the response is 204"
		result.andExpect(status().isNoContent())
		mockMvc.perform(get("/tracked-games/${id}")).andExpect(status().isNotFound())
	}

	def "Given tracked game 99 does not exist, When DELETE /tracked-games/99 is called, Then the response is 404"() {
		when: "DELETE /tracked-games/99 is called"
		def result = mockMvc.perform(delete("/tracked-games/99"))

		then: "the response is 404"
		result.andExpect(status().isNotFound())
				.andExpect(jsonPath('$.status').value(404))
	}

	private long extractLongField(String json, String field) {
		def matcher = (json =~ /"${field}"\s*:\s*(\d+)/)
		matcher.find()
		matcher.group(1) as long
	}

	private void stubRawgGame(long rawgId, String name, String released, String cover) {
		def coverJson = cover == null ? "null" : "\"${cover}\""
		def releasedJson = released == null ? "null" : "\"${released}\""
		wireMock().stubFor(com.github.tomakehurst.wiremock.client.WireMock.get(urlPathEqualTo("/games/${rawgId}"))
				.withQueryParam("key", equalTo("test-key"))
				.willReturn(okJson("""{"id":${rawgId},"name":"${name}","released":${releasedJson},"background_image":${coverJson}}""")))
	}

}
