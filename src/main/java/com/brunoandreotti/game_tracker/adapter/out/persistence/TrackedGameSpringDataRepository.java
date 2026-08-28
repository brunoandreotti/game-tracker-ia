package com.brunoandreotti.game_tracker.adapter.out.persistence;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

interface TrackedGameSpringDataRepository extends JpaRepository<TrackedGameEntity, Long> {

	boolean existsByRawgId(long rawgId);

	List<TrackedGameEntity> findAllByOrderByIdAsc();

}
