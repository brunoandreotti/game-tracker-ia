package com.brunoandreotti.game_tracker.adapter.in.web;

import java.util.List;

import com.brunoandreotti.game_tracker.core.port.in.TrackedGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracked-games")
@RequiredArgsConstructor
public class TrackedGameController {

	private final TrackedGameService trackedGameService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TrackedGameResponse add(@Valid @RequestBody AddTrackedGameRequest request) {
		return TrackedGameResponse.from(trackedGameService.add(request.rawgId(), request.status()));
	}

	@GetMapping
	public List<TrackedGameResponse> list() {
		return trackedGameService.list().stream().map(TrackedGameResponse::from).toList();
	}

	@GetMapping("/{id}")
	public TrackedGameResponse get(@PathVariable long id) {
		return TrackedGameResponse.from(trackedGameService.get(id));
	}

	@PatchMapping("/{id}")
	public TrackedGameResponse patch(@PathVariable long id, @Valid @RequestBody PatchTrackedGameRequest request) {
		return TrackedGameResponse.from(trackedGameService.patch(id, request.status(), request.rating()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		trackedGameService.delete(id);
	}

}
