package com.brunoandreotti.game_tracker.config

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc

@AutoConfigureMockMvc
abstract class RawgMockMvcIntegrationSpec extends RawgWireMockIntegrationSpec {

	WireMockServer wireMock() {
		WireMockTestConfig.server()
	}

}
