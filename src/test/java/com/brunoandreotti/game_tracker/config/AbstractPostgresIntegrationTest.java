package com.brunoandreotti.game_tracker.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@IntegrationTest
public abstract class AbstractPostgresIntegrationTest {

	@Container
	@ServiceConnection
	private static final PostgreSQLContainer POSTGRES = PostgresTestcontainersConfig.newContainer();

}
