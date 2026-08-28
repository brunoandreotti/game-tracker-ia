package com.brunoandreotti.game_tracker.client;

import java.util.List;

import com.brunoandreotti.game_tracker.exception.CatalogUnavailableException;
import com.brunoandreotti.game_tracker.exception.GameNotFoundException;
import com.brunoandreotti.game_tracker.dto.GameSummaryDto;
import com.brunoandreotti.game_tracker.dto.RawgGameDto;
import com.brunoandreotti.game_tracker.dto.RawgSearchResponseDto;
import com.brunoandreotti.game_tracker.config.RawgProperties;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RawgGameCatalogAdapter implements GameCatalogPort {

	private final RawgApiClient rawgApiClient;
	private final RawgProperties rawgProperties;

	@Override
	public List<GameSummaryDto> search(String query) {
		try {
			RawgSearchResponseDto response = rawgApiClient.searchGames(query, rawgProperties.apiKey());
			if (response.results() == null) {
				return List.of();
			}
			return response.results().stream().map(this::toSummary).toList();
		}
		catch (RetryableException exception) {
			throw new CatalogUnavailableException("RAWG unavailable during search", exception);
		}
		catch (FeignException exception) {
			if (exception.status() >= 500) {
				throw new CatalogUnavailableException("RAWG unavailable during search", exception);
			}
			throw new CatalogUnavailableException("RAWG request failed during search", exception);
		}
	}

	@Override
	public GameSummaryDto getByRawgId(long rawgId) {
		try {
			return toSummary(rawgApiClient.getGame(rawgId, rawgProperties.apiKey()));
		}
		catch (FeignException.NotFound exception) {
			throw new GameNotFoundException(rawgId);
		}
		catch (RetryableException exception) {
			throw new CatalogUnavailableException("RAWG unavailable during lookup", exception);
		}
		catch (FeignException exception) {
			if (exception.status() == 404) {
				throw new GameNotFoundException(rawgId);
			}
			if (exception.status() >= 500) {
				throw new CatalogUnavailableException("RAWG unavailable during lookup", exception);
			}
			throw new CatalogUnavailableException("RAWG request failed during lookup", exception);
		}
	}

	private GameSummaryDto toSummary(RawgGameDto game) {
		return new GameSummaryDto(game.id(), game.name(), extractYear(game.released()), game.backgroundImage());
	}

	private Integer extractYear(String released) {
		if (released == null || released.isBlank()) {
			return null;
		}
		return Integer.parseInt(released.substring(0, 4));
	}

}
