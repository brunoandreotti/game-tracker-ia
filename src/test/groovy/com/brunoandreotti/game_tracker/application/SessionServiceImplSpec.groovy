package com.brunoandreotti.game_tracker.application

import com.brunoandreotti.game_tracker.core.exception.InvalidDurationException
import com.brunoandreotti.game_tracker.core.exception.PlaySessionNotFoundException
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException
import com.brunoandreotti.game_tracker.core.model.PlaySession
import com.brunoandreotti.game_tracker.core.model.PlayStatus
import com.brunoandreotti.game_tracker.core.model.TrackedGame
import com.brunoandreotti.game_tracker.core.port.out.PlaySessionRepository
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository
import spock.lang.Specification
import spock.lang.Subject

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId

class SessionServiceImplSpec extends Specification {

	TrackedGameRepository trackedGameRepository = Mock()
	PlaySessionRepository playSessionRepository = Mock()
	Clock clock = Clock.fixed(
			LocalDate.of(2026, 8, 27).atStartOfDay(ZoneId.systemDefault()).toInstant(),
			ZoneId.systemDefault())

	@Subject
	SessionServiceImpl service = new SessionServiceImpl(trackedGameRepository, playSessionRepository, clock)

	def trackedGame = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, null)

	def "Given tracked game 1 exists, When a session is added without playedAt, Then playedAt defaults to today"() {
		when: "a session is added without playedAt"
		def result = service.add(1L, 90, null)

		then: "playedAt defaults to today"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.save({ PlaySession session ->
			session.trackedGameId() == 1L && session.durationMinutes() == 90 && session.playedAt() == LocalDate.of(2026, 8, 27)
		}) >> new PlaySession(10L, 1L, 90, LocalDate.of(2026, 8, 27))
		result.id() == 10L
		result.durationMinutes() == 90
		result.playedAt() == LocalDate.of(2026, 8, 27)
	}

	def "Given tracked game 1 exists, When a session is added with playedAt, Then that date is stored"() {
		given: "a specific playedAt date"
		def playedAt = LocalDate.of(2026, 1, 15)

		when: "a session is added with playedAt"
		def result = service.add(1L, 60, playedAt)

		then: "that date is stored"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.save({ it.playedAt() == playedAt }) >> new PlaySession(11L, 1L, 60, playedAt)
		result.playedAt() == playedAt
	}

	def "Given tracked game 1 is COMPLETED, When a session is added, Then the session is created"() {
		given: "tracked game 1 is COMPLETED"
		def completed = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.COMPLETED, 9)

		when: "a session is added"
		service.add(1L, 30, LocalDate.of(2026, 8, 1))

		then: "the session is created"
		1 * trackedGameRepository.findById(1L) >> Optional.of(completed)
		1 * playSessionRepository.save(_) >> new PlaySession(12L, 1L, 30, LocalDate.of(2026, 8, 1))
	}

	def "Given durationMinutes is zero, When add is called, Then InvalidDurationException is thrown"() {
		when: "add is called with zero duration"
		service.add(1L, 0, null)

		then: "InvalidDurationException is thrown"
		0 * trackedGameRepository._
		thrown(InvalidDurationException)
	}

	def "Given durationMinutes is negative, When add is called, Then InvalidDurationException is thrown"() {
		when: "add is called with negative duration"
		service.add(1L, -5, null)

		then: "InvalidDurationException is thrown"
		thrown(InvalidDurationException)
	}

	def "Given tracked game 99 does not exist, When add is called, Then TrackedGameNotFoundException is thrown"() {
		when: "add is called for tracked game 99"
		service.add(99L, 90, null)

		then: "TrackedGameNotFoundException is thrown"
		1 * trackedGameRepository.findById(99L) >> Optional.empty()
		thrown(TrackedGameNotFoundException)
	}

	def "Given tracked game 1 has sessions, When list is called, Then session views are returned"() {
		given: "tracked game 1 has sessions"
		def sessions = [
				new PlaySession(2L, 1L, 60, LocalDate.of(2026, 8, 26)),
				new PlaySession(1L, 1L, 90, LocalDate.of(2026, 8, 27))
		]

		when: "list is called"
		def results = service.list(1L)

		then: "session views are returned"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.listByTrackedGameId(1L) >> sessions
		results*.id() == [2L, 1L]
	}

	def "Given tracked game 99 does not exist, When list is called, Then TrackedGameNotFoundException is thrown"() {
		when: "list is called for tracked game 99"
		service.list(99L)

		then: "TrackedGameNotFoundException is thrown"
		1 * trackedGameRepository.findById(99L) >> Optional.empty()
		thrown(TrackedGameNotFoundException)
	}

	def "Given session 5 belongs to tracked game 1, When delete is called, Then the session is removed"() {
		given: "session 5 belongs to tracked game 1"
		def session = new PlaySession(5L, 1L, 90, LocalDate.of(2026, 8, 27))

		when: "delete is called"
		service.delete(1L, 5L)

		then: "the session is removed"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.findByIdAndTrackedGameId(5L, 1L) >> Optional.of(session)
		1 * playSessionRepository.delete(session)
	}

	def "Given session 5 belongs to tracked game 2, When delete is called for tracked game 1, Then PlaySessionNotFoundException is thrown"() {
		when: "delete is called for tracked game 1 and session 5"
		service.delete(1L, 5L)

		then: "PlaySessionNotFoundException is thrown"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.findByIdAndTrackedGameId(5L, 1L) >> Optional.empty()
		thrown(PlaySessionNotFoundException)
	}

	def "Given tracked game 1 exists, When two sessions use the same playedAt, Then both sessions are saved"() {
		given: "the same playedAt date"
		def playedAt = LocalDate.of(2026, 8, 27)

		when: "two sessions use the same playedAt"
		service.add(1L, 90, playedAt)
		service.add(1L, 60, playedAt)

		then: "both sessions are saved"
		2 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.save({ it.durationMinutes() == 90 }) >> new PlaySession(20L, 1L, 90, playedAt)
		1 * playSessionRepository.save({ it.durationMinutes() == 60 }) >> new PlaySession(21L, 1L, 60, playedAt)
	}

}
