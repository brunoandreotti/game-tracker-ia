export type PlayStatus = 'WANT_TO_PLAY' | 'PLAYING' | 'COMPLETED' | 'DROPPED'

export interface GameSummaryDto {
  rawgId: number
  name: string
  year: number | null
  coverUrl: string | null
}

export interface TrackedGameDto {
  id: number
  rawgId: number
  name: string
  year: number | null
  coverUrl: string | null
  status: PlayStatus
  rating: number | null
  totalMinutes: number
}

export interface SessionDto {
  id: number
  durationMinutes: number
  playedAt: string
}

export interface ApiErrorBody {
  status: number
  error: string
  message: string
}
