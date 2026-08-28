import type { PlayStatus } from '../api/types'

const PLAY_STATUS_LABELS: Record<PlayStatus, string> = {
  WANT_TO_PLAY: 'Quero jogar',
  PLAYING: 'Jogando',
  COMPLETED: 'Zerei',
  DROPPED: 'Dropado',
}

export function playStatusLabel(status: PlayStatus): string {
  return PLAY_STATUS_LABELS[status]
}

export const PLAY_STATUS_OPTIONS: { value: PlayStatus; label: string }[] = (
  Object.entries(PLAY_STATUS_LABELS) as [PlayStatus, string][]
).map(([value, label]) => ({ value, label }))
