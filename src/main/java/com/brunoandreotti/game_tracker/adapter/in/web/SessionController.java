package com.brunoandreotti.game_tracker.adapter.in.web;

import java.util.List;

import com.brunoandreotti.game_tracker.core.port.in.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tracked-games/{trackedGameId}/sessions")
@RequiredArgsConstructor
public class SessionController {

	private final SessionService sessionService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SessionResponse add(@PathVariable long trackedGameId, @Valid @RequestBody CreateSessionRequest request) {
		return SessionResponse.from(sessionService.add(trackedGameId, request.durationMinutes(), request.playedAt()));
	}

	@GetMapping
	public List<SessionResponse> list(@PathVariable long trackedGameId) {
		return sessionService.list(trackedGameId).stream().map(SessionResponse::from).toList();
	}

	@DeleteMapping("/{sessionId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long trackedGameId, @PathVariable long sessionId) {
		sessionService.delete(trackedGameId, sessionId);
	}

}
