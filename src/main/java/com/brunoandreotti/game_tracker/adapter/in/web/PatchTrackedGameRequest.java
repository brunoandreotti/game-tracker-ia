package com.brunoandreotti.game_tracker.adapter.in.web;

import com.brunoandreotti.game_tracker.core.model.PlayStatus;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PatchTrackedGameRequest(PlayStatus status, @Min(1) @Max(10) Integer rating) {
}
