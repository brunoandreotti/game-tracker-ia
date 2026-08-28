package com.brunoandreotti.game_tracker.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brunoandreotti.game_tracker.entity.TrackedGameEntity;

interface TrackedGameSpringDataRepository extends JpaRepository<TrackedGameEntity, Long> {

	boolean existsByRawgId(long rawgId);

	List<TrackedGameEntity> findAllByOrderByIdAsc();

}
