package com.brunoandreotti.game_tracker.application

import com.brunoandreotti.game_tracker.core.exception.DuplicateRawgIdException
import com.brunoandreotti.game_tracker.core.exception.GameNotFoundException
import com.brunoandreotti.game_tracker.core.exception.InvalidPatchRequestException
import com.brunoandreotti.game_tracker.core.exception.TrackedGameNotFoundException
import com.brunoandreotti.game_tracker.core.model.GameSummary
import com.brunoandreotti.game_tracker.core.model.PlayStatus
import com.brunoandreotti.game_tracker.core.model.TrackedGame
import com.brunoandreotti.game_tracker.core.port.out.GameCatalogPort
import com.brunoandreotti.game_tracker.core.port.out.PlaySessionRepository
import com.brunoandreotti.game_tracker.core.port.out.TrackedGameRepository
import spock.lang.Specification
import spock.lang.Subject

class TrackedGameServiceImplSpec extends Specification {

	GameCatalogPort gameCatalogPort = Mock()
	TrackedGameRepository trackedGameRepository = Mock()
	PlaySessionRepository playSessionRepository = Mock()

	@Subject
	TrackedGameServiceImpl service = new TrackedGameServiceImpl(gameCatalogPort, trackedGameRepository, playSessionRepository)

	def "Given a new rawgId on RAWG, When a tracked game is added without status, Then PLAYING is stored with null rating and zero totalMinutes"() {
		given: "rawg id 123 is not tracked yet and RAWG returns Zelda"
		def summary = new GameSummary(123L, "Zelda", 2017, "https://cover")

		when: "a tracked game is added without status"
		def result = service.add(123L, null)

		then: "PLAYING is stored with null rating and zero totalMinutes"
		1 * trackedGameRepository.existsByRawgId(123L) >> false
		1 * gameCatalogPort.getByRawgId(123L) >> summary
		1 * trackedGameRepository.save({ TrackedGame game ->
			game.rawgId() == 123L && game.name() == "Zelda" && game.status() == PlayStatus.PLAYING && game.rating() == null
		}) >> { TrackedGame game -> new TrackedGame(1L, game.rawgId(), game.name(), game.year(), game.coverUrl(), game.status(), game.rating()) }
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 0
		result.id() == 1L
		result.status() == PlayStatus.PLAYING
		result.rating() == null
		result.totalMinutes() == 0
		result.name() == "Zelda"
		result.year() == 2017
		result.coverUrl() == "https://cover"
	}

	def "Given a new rawgId on RAWG, When a tracked game is added with WANT_TO_PLAY, Then that status is stored"() {
		given: "rawg id 123 is not tracked yet"
		def summary = new GameSummary(123L, "Zelda", 2017, "https://cover")

		when: "a tracked game is added with WANT_TO_PLAY"
		def result = service.add(123L, PlayStatus.WANT_TO_PLAY)

		then: "WANT_TO_PLAY is stored"
		1 * trackedGameRepository.existsByRawgId(123L) >> false
		1 * gameCatalogPort.getByRawgId(123L) >> summary
		1 * trackedGameRepository.save({ it.status() == PlayStatus.WANT_TO_PLAY }) >> { TrackedGame game ->
			new TrackedGame(2L, game.rawgId(), game.name(), game.year(), game.coverUrl(), game.status(), game.rating())
		}
		1 * playSessionRepository.sumDurationByTrackedGameId(2L) >> 0
		result.status() == PlayStatus.WANT_TO_PLAY
	}

	def "Given rawgId 123 is already tracked, When add is called again, Then DuplicateRawgIdException is thrown"() {
		when: "add is called again for rawgId 123"
		service.add(123L, null)

		then: "DuplicateRawgIdException is thrown"
		1 * trackedGameRepository.existsByRawgId(123L) >> true
		0 * gameCatalogPort._
		thrown(DuplicateRawgIdException)
	}

	def "Given rawgId 999 is missing on RAWG, When add is called, Then GameNotFoundException is thrown"() {
		when: "add is called for rawgId 999"
		service.add(999L, null)

		then: "GameNotFoundException is thrown"
		1 * trackedGameRepository.existsByRawgId(999L) >> false
		1 * gameCatalogPort.getByRawgId(999L) >> { throw new GameNotFoundException(999L) }
		thrown(GameNotFoundException)
	}

	def "Given two tracked games exist, When list is called, Then entries are returned with computed totalMinutes"() {
		given: "two tracked games exist"
		def first = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, null)
		def second = new TrackedGame(2L, 456L, "Mario", 2020, null, PlayStatus.COMPLETED, 5)

		when: "list is called"
		def results = service.list()

		then: "entries are returned with computed totalMinutes"
		1 * trackedGameRepository.findAllOrderByIdAsc() >> [first, second]
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 150
		1 * playSessionRepository.sumDurationByTrackedGameId(2L) >> 0
		results*.id() == [1L, 2L]
		results[0].totalMinutes() == 150
		results[1].rating() == 5
	}

	def "Given tracked game 1 exists, When get is called for id 1, Then the tracked game view is returned"() {
		given: "tracked game 1 exists"
		def trackedGame = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, null)

		when: "get is called for id 1"
		def result = service.get(1L)

		then: "the tracked game view is returned"
		1 * trackedGameRepository.findById(1L) >> Optional.of(trackedGame)
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 90
		result.totalMinutes() == 90
	}

	def "Given tracked game 99 does not exist, When get is called for id 99, Then TrackedGameNotFoundException is thrown"() {
		when: "get is called for id 99"
		service.get(99L)

		then: "TrackedGameNotFoundException is thrown"
		1 * trackedGameRepository.findById(99L) >> Optional.empty()
		thrown(TrackedGameNotFoundException)
	}

	def "Given tracked game 1 exists with rating 4, When patch sets only status to COMPLETED, Then rating stays 4"() {
		given: "tracked game 1 exists with rating 4"
		def existing = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, 4)

		when: "patch sets only status to COMPLETED"
		def result = service.patch(1L, PlayStatus.COMPLETED, null)

		then: "rating stays 4"
		1 * trackedGameRepository.findById(1L) >> Optional.of(existing)
		1 * trackedGameRepository.save({ TrackedGame game ->
			game.status() == PlayStatus.COMPLETED && game.rating() == 4
		}) >> { TrackedGame game -> game }
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 0
		result.status() == PlayStatus.COMPLETED
		result.rating() == 4
	}

	def "Given tracked game 1 is COMPLETED, When patch sets only rating to 5, Then status stays COMPLETED"() {
		given: "tracked game 1 is COMPLETED"
		def existing = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.COMPLETED, null)

		when: "patch sets only rating to 5"
		def result = service.patch(1L, null, 5)

		then: "status stays COMPLETED"
		1 * trackedGameRepository.findById(1L) >> Optional.of(existing)
		1 * trackedGameRepository.save({ it.status() == PlayStatus.COMPLETED && it.rating() == 5 }) >> { TrackedGame game -> game }
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 0
		result.rating() == 5
	}

	def "Given tracked game 1 is COMPLETED, When patch sets status to WANT_TO_PLAY, Then the transition is allowed"() {
		given: "tracked game 1 is COMPLETED"
		def existing = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.COMPLETED, 5)

		when: "patch sets status to WANT_TO_PLAY"
		def result = service.patch(1L, PlayStatus.WANT_TO_PLAY, null)

		then: "the transition is allowed"
		1 * trackedGameRepository.findById(1L) >> Optional.of(existing)
		1 * trackedGameRepository.save({ it.status() == PlayStatus.WANT_TO_PLAY && it.rating() == 5 }) >> { TrackedGame game -> game }
		1 * playSessionRepository.sumDurationByTrackedGameId(1L) >> 0
		result.status() == PlayStatus.WANT_TO_PLAY
	}

	def "Given tracked game 1 exists, When patch is called with no status and no rating, Then InvalidPatchRequestException is thrown"() {
		when: "patch is called with no status and no rating"
		service.patch(1L, null, null)

		then: "InvalidPatchRequestException is thrown"
		0 * trackedGameRepository._
		thrown(InvalidPatchRequestException)
	}

	def "Given tracked game 1 exists, When delete is called for id 1, Then the repository deletes it"() {
		given: "tracked game 1 exists"
		def existing = new TrackedGame(1L, 123L, "Zelda", 2017, "https://cover", PlayStatus.PLAYING, null)

		when: "delete is called for id 1"
		service.delete(1L)

		then: "the repository deletes it"
		1 * trackedGameRepository.findById(1L) >> Optional.of(existing)
		1 * trackedGameRepository.deleteById(1L)
	}

	def "Given tracked game 99 does not exist, When delete is called for id 99, Then TrackedGameNotFoundException is thrown"() {
		when: "delete is called for id 99"
		service.delete(99L)

		then: "TrackedGameNotFoundException is thrown"
		1 * trackedGameRepository.findById(99L) >> Optional.empty()
		0 * trackedGameRepository.deleteById(_)
		thrown(TrackedGameNotFoundException)
	}

}
