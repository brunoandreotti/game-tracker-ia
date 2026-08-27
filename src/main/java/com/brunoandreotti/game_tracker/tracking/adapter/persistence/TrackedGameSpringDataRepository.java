package com.brunoandreotti.game_tracker.tracking.adapter.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface TrackedGameSpringDataRepository extends JpaRepository<TrackedGameEntity, Long> {

	boolean existsByRawgId(long rawgId);

	List<TrackedGameEntity> findAllByOrderByIdAsc();

}
