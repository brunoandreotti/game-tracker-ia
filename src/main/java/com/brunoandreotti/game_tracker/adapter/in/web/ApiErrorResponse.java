package com.brunoandreotti.game_tracker.adapter.in.web;

public record ApiErrorResponse(int status, String error, String message) {
}
