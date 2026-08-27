package com.brunoandreotti.game_tracker.config

import com.github.tomakehurst.wiremock.WireMockServer

@WithRawgWireMock
abstract class RawgWireMockIntegrationSpec extends PostgresIntegrationSpec {

	WireMockServer wireMock() {
		WireMockTestConfig.server()
	}

	def setup() {
		WireMockTestConfig.reset()
	}

}
