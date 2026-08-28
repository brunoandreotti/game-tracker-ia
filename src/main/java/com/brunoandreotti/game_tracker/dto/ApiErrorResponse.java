package com.brunoandreotti.game_tracker.dto;

public record ApiErrorResponse(int status, String error, String message) {
}
