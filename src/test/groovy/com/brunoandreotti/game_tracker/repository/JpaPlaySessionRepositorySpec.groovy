package com.brunoandreotti.game_tracker.repository

import com.brunoandreotti.game_tracker.config.PostgresIntegrationSpec
import com.brunoandreotti.game_tracker.repository.PlaySessionRepository
import com.brunoandreotti.game_tracker.repository.TrackedGameRepository
import com.brunoandreotti.game_tracker.model.PlaySession
import com.brunoandreotti.game_tracker.model.PlayStatus
import com.brunoandreotti.game_tracker.model.TrackedGame
import org.springframework.beans.factory.annotation.Autowired

import java.time.LocalDate

class JpaPlaySessionRepositorySpec extends PostgresIntegrationSpec {

	@Autowired
	TrackedGameRepository trackedGameRepository

	@Autowired
	PlaySessionRepository playSessionRepository

	@Autowired
	PlaySessionSpringDataRepository playSessionSpringDataRepository

	def "Given two sessions on different dates, When sessions are listed for the tracked game, Then they are ordered by playedAt desc then id desc"() {
		given: "two sessions on different dates for the same tracked game"
		def trackedGame = trackedGameRepository.save(
				new TrackedGame(null, 789L, "Ordering", null, null, PlayStatus.PLAYING, null))
		def older = playSessionRepository.save(
				new PlaySession(null, trackedGame.id(), 60, LocalDate.of(2026, 1, 1)))
		def newer = playSessionRepository.save(
				new PlaySession(null, trackedGame.id(), 90, LocalDate.of(2026, 2, 1)))

		when: "sessions are listed for the tracked game"
		def sessions = playSessionRepository.listByTrackedGameId(trackedGame.id())

		then: "they are ordered by playedAt desc then id desc"
		sessions*.id() == [newer.id(), older.id()]
		sessions*.durationMinutes() == [90, 60]
	}

	def "Given a tracked game with sessions, When the tracked game is deleted, Then its sessions are removed"() {
		given: "a tracked game with sessions"
		def trackedGame = trackedGameRepository.save(
				new TrackedGame(null, 999L, "Cascade", null, null, PlayStatus.PLAYING, null))
		playSessionRepository.save(new PlaySession(null, trackedGame.id(), 45, LocalDate.of(2026, 3, 1)))

		when: "the tracked game is deleted"
		trackedGameRepository.deleteById(trackedGame.id())

		then: "its sessions are removed"
		playSessionSpringDataRepository.findByTrackedGame_IdOrderByPlayedAtDescIdDesc(trackedGame.id()).isEmpty()
	}

}
