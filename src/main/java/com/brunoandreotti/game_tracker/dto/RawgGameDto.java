package com.brunoandreotti.game_tracker.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgGameDto(
		long id,
		String name,
		String released,
		@JsonProperty("background_image") String backgroundImage) {
}
