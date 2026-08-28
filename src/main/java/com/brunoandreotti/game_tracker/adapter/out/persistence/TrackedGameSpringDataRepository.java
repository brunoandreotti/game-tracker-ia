package com.brunoandreotti.game_tracker.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brunoandreotti.game_tracker.adapter.out.persistence.TrackedGameEntity;

interface TrackedGameSpringDataRepository extends JpaRepository<TrackedGameEntity, Long> {

	boolean existsByRawgId(long rawgId);

	List<TrackedGameEntity> findAllByOrderByIdAsc();

}
