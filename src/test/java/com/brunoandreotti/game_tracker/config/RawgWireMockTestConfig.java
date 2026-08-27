package com.brunoandreotti.game_tracker.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public final class RawgWireMockTestConfig {

	private RawgWireMockTestConfig() {
	}

	public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext context) {
			WireMockTestConfig.propertyInitializer(
					"rawg.base-url=" + WireMockTestConfig.server().baseUrl(),
					"rawg.api-key=test-key",
					"spring.cloud.openfeign.client.config.rawg.connect-timeout=500",
					"spring.cloud.openfeign.client.config.rawg.read-timeout=500"
			).initialize(context);
		}
	}

}
