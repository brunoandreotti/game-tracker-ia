package com.brunoandreotti.game_tracker.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rawg")
public record RawgProperties(String apiKey) {
}
