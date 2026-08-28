package com.brunoandreotti.game_tracker.adapter.out.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "play_session")
@Getter
@Setter
public class PlaySessionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tracked_game_id", nullable = false)
	private TrackedGameEntity trackedGame;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	@Column(name = "played_at", nullable = false)
	private LocalDate playedAt;

}
