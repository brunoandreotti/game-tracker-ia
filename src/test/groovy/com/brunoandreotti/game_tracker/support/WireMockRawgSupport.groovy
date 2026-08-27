package com.brunoandreotti.game_tracker.support

import com.github.tomakehurst.wiremock.WireMockServer
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext

final class WireMockRawgSupport {

	static final WireMockServer WIRE_MOCK = new WireMockServer(0)

	static {
		WIRE_MOCK.start()
	}

	static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		void initialize(ConfigurableApplicationContext context) {
			TestPropertyValues.of(
					"spring.http.serviceclient.rawg.base-url=" + WIRE_MOCK.baseUrl(),
					"rawg.api-key=test-key",
					"spring.http.serviceclient.rawg.connect-timeout=500ms",
					"spring.http.serviceclient.rawg.read-timeout=500ms"
			).applyTo(context.environment)
		}
	}
}
