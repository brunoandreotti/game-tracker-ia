package com.brunoandreotti.game_tracker.tracking.adapter.persistence;

import java.util.ArrayList;
import java.util.List;

import com.brunoandreotti.game_tracker.tracking.domain.PlayStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tracked_game")
@Getter
@Setter
public class TrackedGameEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "rawg_id", nullable = false, unique = true)
	private long rawgId;

	@Column(nullable = false)
	private String name;

	private Integer year;

	@Column(name = "cover_url")
	private String coverUrl;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private PlayStatus status;

	private Integer rating;

	@OneToMany(mappedBy = "trackedGame", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PlaySessionEntity> sessions = new ArrayList<>();

}
