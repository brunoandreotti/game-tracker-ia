package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.PlayStatus;

import jakarta.validation.constraints.NotNull;

public record AddTrackedGameRequest(@NotNull Long rawgId, PlayStatus status) {
}
