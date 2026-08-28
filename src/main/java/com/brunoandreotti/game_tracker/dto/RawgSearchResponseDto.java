package com.brunoandreotti.game_tracker.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RawgSearchResponseDto(List<RawgGameDto> results) {
}
