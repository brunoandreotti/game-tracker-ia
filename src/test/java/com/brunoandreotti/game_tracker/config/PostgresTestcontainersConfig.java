package com.brunoandreotti.game_tracker.config;

import org.testcontainers.postgresql.PostgreSQLContainer;

public final class PostgresTestcontainersConfig {

	public static final String IMAGE = "postgres:16-alpine";

	private PostgresTestcontainersConfig() {
	}

	public static PostgreSQLContainer newContainer() {
		return new PostgreSQLContainer(IMAGE);
	}

}
