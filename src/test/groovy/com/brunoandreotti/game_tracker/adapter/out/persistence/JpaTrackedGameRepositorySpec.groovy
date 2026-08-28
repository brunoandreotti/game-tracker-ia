package com.brunoandreotti.game_tracker.adapter.out.persistence

import com.brunoandreotti.game_tracker.config.PostgresIntegrationSpec
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository
import com.brunoandreotti.game_tracker.core.exception.DuplicateRawgIdException
import com.brunoandreotti.game_tracker.core.model.PlayStatus
import com.brunoandreotti.game_tracker.core.model.TrackedGame
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate

class JpaTrackedGameRepositorySpec extends PostgresIntegrationSpec {

	@Autowired
	TrackedGameRepository trackedGameRepository

	@Autowired
	JdbcTemplate jdbcTemplate

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

	def "Given ratings 0, 5, and null, When saved via the repository, Then they persist successfully"() {
		when: "games with valid ratings are saved"
		def zeroRating = trackedGameRepository.save(new TrackedGame(null, 902L, "Zero", null, null, PlayStatus.PLAYING, 0))
		def fiveRating = trackedGameRepository.save(new TrackedGame(null, 903L, "Five", null, null, PlayStatus.PLAYING, 5))
		def nullRating = trackedGameRepository.save(new TrackedGame(null, 904L, "Unset", null, null, PlayStatus.PLAYING, null))

		then: "the stored ratings match"
		trackedGameRepository.findById(zeroRating.id()).get().rating() == 0
		trackedGameRepository.findById(fiveRating.id()).get().rating() == 5
		trackedGameRepository.findById(nullRating.id()).get().rating() == null
	}

	def "Given legacy rating 9 exists, When the V2 clamp UPDATE runs, Then the stored rating is 5"() {
		given: "legacy rating 9 exists without the CHECK constraint"
		jdbcTemplate.execute("ALTER TABLE tracked_game DROP CONSTRAINT IF EXISTS chk_tracked_game_rating_range")
		jdbcTemplate.update(
				"INSERT INTO tracked_game (rawg_id, name, status, rating) VALUES (?, ?, ?, ?)",
				905L, "Legacy High", "PLAYING", 9)

		when: "the V2 clamp UPDATE runs"
		jdbcTemplate.update("UPDATE tracked_game SET rating = 5 WHERE rating > 5")
		jdbcTemplate.update("UPDATE tracked_game SET rating = 0 WHERE rating < 0")

		then: "the stored rating is 5"
		jdbcTemplate.queryForObject("SELECT rating FROM tracked_game WHERE rawg_id = 905", Integer) == 5

		cleanup:
		jdbcTemplate.update("DELETE FROM tracked_game WHERE rawg_id = 905")
		jdbcTemplate.execute("ALTER TABLE tracked_game DROP CONSTRAINT IF EXISTS chk_tracked_game_rating_range")
		jdbcTemplate.execute("""
			ALTER TABLE tracked_game
				ADD CONSTRAINT chk_tracked_game_rating_range
					CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5))
		""")
	}

	def "Given the rating CHECK constraint is active, When rating 9 is inserted via SQL, Then the insert fails"() {
		when: "a row with rating 9 is inserted via SQL"
		jdbcTemplate.update(
				"INSERT INTO tracked_game (rawg_id, name, status, rating) VALUES (?, ?, ?, ?)",
				906L, "Bad Rating", "PLAYING", 9)

		then: "the insert fails due to CHECK constraint"
		thrown(DataIntegrityViolationException)
	}

	def "Given the rating CHECK constraint is active, When rating -1 is inserted via SQL, Then the insert fails"() {
		when: "a row with rating -1 is inserted via SQL"
		jdbcTemplate.update(
				"INSERT INTO tracked_game (rawg_id, name, status, rating) VALUES (?, ?, ?, ?)",
				907L, "Negative Rating", "PLAYING", -1)

		then: "the insert fails due to CHECK constraint"
		thrown(DataIntegrityViolationException)
	}

}
