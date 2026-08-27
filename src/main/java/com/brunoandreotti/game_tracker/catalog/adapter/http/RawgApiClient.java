package com.brunoandreotti.game_tracker.catalog.adapter.http;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(accept = "application/json")
public interface RawgApiClient {

	@GetExchange("/games")
	RawgSearchResponse searchGames(@RequestParam("search") String search, @RequestParam("key") String apiKey);

	@GetExchange("/games/{id}")
	RawgGameResponse getGame(@PathVariable("id") long id, @RequestParam("key") String apiKey);
}
