package com.brunoandreotti.game_tracker.catalog.adapter.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgGameResponse(
		long id,
		String name,
		String released,
		@JsonProperty("background_image") String backgroundImage) {
}
