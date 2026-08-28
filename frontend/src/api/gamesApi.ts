import { apiRequest } from './apiClient'
import type { GameSummaryDto, PlayStatus, SessionDto, TrackedGameDto } from './types'

export function searchGames(q: string, exact?: boolean): Promise<GameSummaryDto[]> {
  const params = new URLSearchParams({ q })
  if (exact) {
    params.set('exact', 'true')
  }
  return apiRequest<GameSummaryDto[]>(`/games/search?${params.toString()}`)
}

export function listTrackedGames(): Promise<TrackedGameDto[]> {
  return apiRequest<TrackedGameDto[]>('/tracked-games')
}

export function getTrackedGame(id: number): Promise<TrackedGameDto> {
  return apiRequest<TrackedGameDto>(`/tracked-games/${id}`)
}

export function createTrackedGame(rawgId: number): Promise<TrackedGameDto> {
  return apiRequest<TrackedGameDto>('/tracked-games', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ rawgId }),
  })
}

export function patchTrackedGame(
  id: number,
  payload: { status?: PlayStatus; rating?: number },
): Promise<TrackedGameDto> {
  return apiRequest<TrackedGameDto>(`/tracked-games/${id}`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
}

export function deleteTrackedGame(id: number): Promise<void> {
  return apiRequest<void>(`/tracked-games/${id}`, { method: 'DELETE' })
}

export function listSessions(trackedGameId: number): Promise<SessionDto[]> {
  return apiRequest<SessionDto[]>(`/tracked-games/${trackedGameId}/sessions`)
}

export function createSession(
  trackedGameId: number,
  payload: { durationMinutes: number; playedAt?: string },
): Promise<SessionDto> {
  return apiRequest<SessionDto>(`/tracked-games/${trackedGameId}/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
}

export function deleteSession(trackedGameId: number, sessionId: number): Promise<void> {
  return apiRequest<void>(`/tracked-games/${trackedGameId}/sessions/${sessionId}`, {
    method: 'DELETE',
  })
}
