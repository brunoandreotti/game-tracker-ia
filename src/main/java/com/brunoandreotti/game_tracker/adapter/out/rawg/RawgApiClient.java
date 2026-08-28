package com.brunoandreotti.game_tracker.adapter.out.rawg;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rawg", url = "${rawg.base-url}")
public interface RawgApiClient {

	@GetMapping("/games")
	RawgSearchResponseDto searchGames(
			@RequestParam("search") String search,
			@RequestParam("search_precise") boolean searchPrecise,
			@RequestParam("search_exact") boolean searchExact,
			@RequestParam("key") String apiKey);

	@GetMapping("/games/{id}")
	RawgGameDto getGame(@PathVariable long id, @RequestParam("key") String apiKey);

}
