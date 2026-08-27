package com.brunoandreotti.game_tracker.catalog.config;

import com.brunoandreotti.game_tracker.catalog.adapter.http.RawgApiClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.service.registry.ImportHttpServices;

@Configuration
@ImportHttpServices(group = "rawg", types = RawgApiClient.class)
@EnableConfigurationProperties(RawgProperties.class)
public class RawgHttpConfig {
}
