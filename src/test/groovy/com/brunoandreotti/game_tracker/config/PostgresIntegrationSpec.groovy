package com.brunoandreotti.game_tracker.config

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.postgresql.PostgreSQLContainer
import spock.lang.Specification

@IntegrationTest
abstract class PostgresIntegrationSpec extends Specification {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = PostgresTestcontainersConfig.newContainer()

}
