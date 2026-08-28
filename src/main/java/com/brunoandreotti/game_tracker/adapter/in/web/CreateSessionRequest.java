package com.brunoandreotti.game_tracker.adapter.in.web;

import java.time.LocalDate;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateSessionRequest(@NotNull @Min(1) Integer durationMinutes, LocalDate playedAt) {
}
