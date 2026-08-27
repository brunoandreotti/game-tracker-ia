package com.brunoandreotti.game_tracker.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

public final class WireMockTestConfig {

	private static final WireMockServer SERVER = new WireMockServer(0);

	static {
		SERVER.start();
	}

	private WireMockTestConfig() {
	}

	public static WireMockServer server() {
		return SERVER;
	}

	public static void reset() {
		SERVER.resetAll();
	}

	public static ApplicationContextInitializer<ConfigurableApplicationContext> propertyInitializer(String... properties) {
		return context -> TestPropertyValues.of(properties).applyTo(context.getEnvironment());
	}

}
