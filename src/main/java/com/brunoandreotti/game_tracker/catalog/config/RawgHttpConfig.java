package com.brunoandreotti.game_tracker.catalog.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.brunoandreotti.game_tracker.catalog.adapter.http")
@EnableConfigurationProperties(RawgProperties.class)
public class RawgHttpConfig {
}
