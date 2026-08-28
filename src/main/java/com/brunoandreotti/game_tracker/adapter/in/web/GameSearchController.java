package com.brunoandreotti.game_tracker.adapter.in.web;

import java.util.List;

import com.brunoandreotti.game_tracker.core.port.in.GameSearchService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
@Validated
@RequiredArgsConstructor
public class GameSearchController {

	private final GameSearchService gameSearchService;

	@GetMapping("/search")
	public List<GameSearchResponse> search(@RequestParam @NotBlank String q) {
		return gameSearchService.search(q).stream()
				.map(GameSearchResponse::from)
				.toList();
	}
}
