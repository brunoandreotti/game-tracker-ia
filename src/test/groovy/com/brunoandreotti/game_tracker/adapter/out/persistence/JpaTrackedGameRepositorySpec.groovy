package com.brunoandreotti.game_tracker.adapter.out.persistence

import com.brunoandreotti.game_tracker.config.PostgresIntegrationSpec
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository
import com.brunoandreotti.game_tracker.core.exception.DuplicateRawgIdException
import com.brunoandreotti.game_tracker.core.model.PlayStatus
import com.brunoandreotti.game_tracker.core.model.TrackedGame
import org.springframework.beans.factory.annotation.Autowired

class JpaTrackedGameRepositorySpec extends PostgresIntegrationSpec {

	@Autowired
	TrackedGameRepository trackedGameRepository

	def "Given no tracked game exists for rawg id 123, When a Zelda game is saved and loaded by id, Then the stored fields match"() {
		given: "no tracked game exists for rawg id 123"
		def game = new TrackedGame(null, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, null)

		when: "the game is saved and loaded by id"
		def saved = trackedGameRepository.save(game)
		def loaded = trackedGameRepository.findById(saved.id())

		then: "the stored fields match"
		loaded.isPresent()
		with(loaded.get()) {
			rawgId() == 123L
			name() == "Zelda"
			year() == 2017
			coverUrl() == "https://cover"
			status() == PlayStatus.PLAYING
			rating() == null
		}
	}

	def "Given a tracked game exists for rawg id 456, When existsByRawgId is checked, Then it returns true and a duplicate save throws DuplicateRawgIdException"() {
		given: "a tracked game exists for rawg id 456"
		trackedGameRepository.save(new TrackedGame(null, 456L, "Duplicate", null, null, PlayStatus.PLAYING, null))

		when: "existsByRawgId is checked and a duplicate save is attempted"
		def exists = trackedGameRepository.existsByRawgId(456L)
		trackedGameRepository.save(new TrackedGame(null, 456L, "Another", null, null, PlayStatus.PLAYING, null))

		then: "existsByRawgId returns true and the duplicate save throws DuplicateRawgIdException"
		exists
		thrown(DuplicateRawgIdException)
	}

}
