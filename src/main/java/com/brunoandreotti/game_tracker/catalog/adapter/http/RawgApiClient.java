package com.brunoandreotti.game_tracker.catalog.adapter.http;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rawg", url = "${rawg.base-url}")
public interface RawgApiClient {

	@GetMapping("/games")
	RawgSearchResponseDto searchGames(@RequestParam("search") String search, @RequestParam("key") String apiKey);

	@GetMapping("/games/{id}")
	RawgGameDto getGame(@PathVariable("id") long id, @RequestParam("key") String apiKey);

}
