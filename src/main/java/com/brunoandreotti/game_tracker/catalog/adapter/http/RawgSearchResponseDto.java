package com.brunoandreotti.game_tracker.catalog.adapter.http;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgSearchResponseDto(List<RawgGameDto> results) {
}
