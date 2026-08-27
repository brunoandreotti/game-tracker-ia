package com.brunoandreotti.game_tracker.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.BeforeEach;

@WithRawgWireMock
public abstract class AbstractRawgWireMockIntegrationTest extends AbstractPostgresIntegrationTest {

	protected WireMockServer wireMock() {
		return WireMockTestConfig.server();
	}

	@BeforeEach
	void resetWireMock() {
		WireMockTestConfig.reset();
	}

}
