package com.brunoandreotti.game_tracker.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


interface PlaySessionSpringDataRepository extends JpaRepository<PlaySessionEntity, Long> {

	List<PlaySessionEntity> findByTrackedGame_IdOrderByPlayedAtDescIdDesc(long trackedGameId);

	Optional<PlaySessionEntity> findByIdAndTrackedGameId(long id, long trackedGameId);

	@Query("SELECT COALESCE(SUM(p.durationMinutes), 0) FROM PlaySessionEntity p WHERE p.trackedGame.id = :trackedGameId")
	int sumDurationByTrackedGameId(@Param("trackedGameId") long trackedGameId);

}
