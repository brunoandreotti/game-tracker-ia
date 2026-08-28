package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.PlayStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PatchTrackedGameRequest(PlayStatus status, @Min(0) @Max(5) Integer rating) {
}
