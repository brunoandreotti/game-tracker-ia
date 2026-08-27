package com.brunoandreotti.game_tracker.catalog.adapter.http;

import java.util.List;

import com.brunoandreotti.game_tracker.catalog.application.CatalogUnavailableException;
import com.brunoandreotti.game_tracker.catalog.application.GameCatalog;
import com.brunoandreotti.game_tracker.catalog.application.GameNotFoundException;
import com.brunoandreotti.game_tracker.catalog.application.GameSummary;
import com.brunoandreotti.game_tracker.catalog.config.RawgProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Service
@RequiredArgsConstructor
public class RawgGameCatalog implements GameCatalog {

	private final RawgApiClient rawgApiClient;
	private final RawgProperties rawgProperties;

	@Override
	public List<GameSummary> search(String query) {
		try {
			RawgSearchResponse response = rawgApiClient.searchGames(query, rawgProperties.apiKey());
			if (response.results() == null) {
				return List.of();
			}
			return response.results().stream().map(this::toSummary).toList();
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().is5xxServerError()) {
				throw new CatalogUnavailableException("RAWG unavailable during search", exception);
			}
			throw new CatalogUnavailableException("RAWG request failed during search", exception);
		}
		catch (RestClientException exception) {
			throw new CatalogUnavailableException("RAWG unavailable during search", exception);
		}
	}

	@Override
	public GameSummary getByRawgId(long rawgId) {
		try {
			return toSummary(rawgApiClient.getGame(rawgId, rawgProperties.apiKey()));
		}
		catch (HttpClientErrorException.NotFound exception) {
			throw new GameNotFoundException(rawgId);
		}
		catch (RestClientResponseException exception) {
			if (exception.getStatusCode().value() == 404) {
				throw new GameNotFoundException(rawgId);
			}
			if (exception.getStatusCode().is5xxServerError()) {
				throw new CatalogUnavailableException("RAWG unavailable during lookup", exception);
			}
			throw new CatalogUnavailableException("RAWG request failed during lookup", exception);
		}
		catch (RestClientException exception) {
			throw new CatalogUnavailableException("RAWG unavailable during lookup", exception);
		}
	}

	private GameSummary toSummary(RawgGameResponse game) {
		return new GameSummary(game.id(), game.name(), extractYear(game.released()), game.backgroundImage());
	}

	private Integer extractYear(String released) {
		if (released == null || released.isBlank()) {
			return null;
		}
		return Integer.parseInt(released.substring(0, 4));
	}
}
