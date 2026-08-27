package com.brunoandreotti.game_tracker.config;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

@IntegrationTest
public abstract class AbstractPostgresIntegrationTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer<?> postgres = PostgresTestcontainersConfig.newContainer();

}
