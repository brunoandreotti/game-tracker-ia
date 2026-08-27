package com.brunoandreotti.game_tracker.config;

public record ApiErrorResponse(int status, String error, String message) {
}
