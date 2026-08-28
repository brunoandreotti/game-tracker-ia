import { type FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'

import { ApiError } from '../api/apiClient'
import {
  createSession,
  deleteSession,
  deleteTrackedGame,
  getTrackedGame,
  listSessions,
  patchTrackedGame,
} from '../api/gamesApi'
import type { PlayStatus, SessionDto, TrackedGameDto } from '../api/types'
import { CoverImage, ErrorMessage, LoadingMessage } from '../components/Feedback'
import { formatMinutes } from '../lib/formatMinutes'
import { PLAY_STATUS_OPTIONS, playStatusLabel } from '../lib/playStatus'

export function TrackedGameDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const gameId = Number(id)

  const [game, setGame] = useState<TrackedGameDto | null>(null)
  const [sessions, setSessions] = useState<SessionDto[]>([])
  const [loading, setLoading] = useState(true)
  const [notFound, setNotFound] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [mutationError, setMutationError] = useState<string | null>(null)

  const [durationMinutes, setDurationMinutes] = useState('')
  const [playedAt, setPlayedAt] = useState('')
  const [sessionValidationError, setSessionValidationError] = useState<string | null>(null)

  useEffect(() => {
    if (!id || Number.isNaN(gameId)) {
      setNotFound(true)
      setLoading(false)
      return
    }

    let cancelled = false

    async function loadGame() {
      setLoading(true)
      setNotFound(false)
      setError(null)

      try {
        const [gameData, sessionsData] = await Promise.all([
          getTrackedGame(gameId),
          listSessions(gameId),
        ])

        if (!cancelled) {
          setGame(gameData)
          setSessions(sessionsData)
        }
      } catch (err) {
        if (!cancelled) {
          if (err instanceof ApiError && err.status === 404) {
            setNotFound(true)
          } else {
            const message = err instanceof ApiError ? err.message : 'Erro ao carregar jogo.'
            setError(message)
          }
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadGame()

    return () => {
      cancelled = true
    }
  }, [gameId, id])

  async function refreshGameAndSessions() {
    const [gameData, sessionsData] = await Promise.all([
      getTrackedGame(gameId),
      listSessions(gameId),
    ])
    setGame(gameData)
    setSessions(sessionsData)
  }

  async function handleStatusChange(status: PlayStatus) {
    if (!game) {
      return
    }

    setMutationError(null)

    try {
      const updated = await patchTrackedGame(gameId, { status })
      setGame(updated)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao atualizar status.'
      setMutationError(message)
    }
  }

  async function handleRatingChange(ratingValue: string) {
    if (!game) {
      return
    }

    setMutationError(null)

    if (ratingValue === '') {
      return
    }

    const rating = Number(ratingValue)
    if (Number.isNaN(rating) || rating < 1 || rating > 10) {
      setMutationError('A nota deve ser um número entre 1 e 10.')
      return
    }

    try {
      const updated = await patchTrackedGame(gameId, { rating })
      setGame(updated)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao atualizar nota.'
      setMutationError(message)
    }
  }

  async function handleCreateSession(event: FormEvent) {
    event.preventDefault()
    setSessionValidationError(null)
    setMutationError(null)

    const parsed = Number(durationMinutes)
    if (!durationMinutes.trim() || Number.isNaN(parsed) || parsed <= 0) {
      setSessionValidationError('Informe uma duração em minutos maior que zero.')
      return
    }

    const payload: { durationMinutes: number; playedAt?: string } = {
      durationMinutes: parsed,
    }

    if (playedAt.trim()) {
      payload.playedAt = playedAt
    }

    try {
      await createSession(gameId, payload)
      setDurationMinutes('')
      setPlayedAt('')
      await refreshGameAndSessions()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao registrar sessão.'
      setMutationError(message)
    }
  }

  async function handleDeleteSession(sessionId: number) {
    if (!window.confirm('Remover esta sessão?')) {
      return
    }

    setMutationError(null)

    try {
      await deleteSession(gameId, sessionId)
      await refreshGameAndSessions()
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao remover sessão.'
      setMutationError(message)
    }
  }

  async function handleDeleteGame() {
    if (!window.confirm('Remover este jogo da sua lista?')) {
      return
    }

    setMutationError(null)

    try {
      await deleteTrackedGame(gameId)
      navigate('/')
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao remover jogo.'
      setMutationError(message)
    }
  }

  if (loading) {
    return <LoadingMessage message="Carregando jogo..." />
  }

  if (notFound) {
    return (
      <div className="empty-state">
        <p>Jogo não encontrado.</p>
        <div className="empty-state__cta">
          <Link to="/" className="btn">
            Voltar para meus jogos
          </Link>
        </div>
      </div>
    )
  }

  if (error || !game) {
    return <ErrorMessage message={error ?? 'Erro ao carregar jogo.'} />
  }

  return (
    <section>
      <p className="detail-back">
        <Link to="/">← Voltar para meus jogos</Link>
      </p>

      <div className="detail-header">
        <CoverImage src={game.coverUrl} alt={game.name} width={80} height={112} />
        <div className="detail-meta">
          <h1>{game.name}</h1>
          <p className="game-list__details">
            {game.year !== null ? game.year : 'Ano desconhecido'}
            {' · '}
            {playStatusLabel(game.status)}
            {' · '}
            {game.rating !== null ? `Nota ${game.rating}` : 'Sem nota'}
            {' · '}
            {formatMinutes(game.totalMinutes)}
          </p>
        </div>
      </div>

      {mutationError && <ErrorMessage message={mutationError} />}

      <div className="detail-section">
        <h2>Status e nota</h2>
        <div className="detail-controls">
          <label>
            Status
            <select
              value={game.status}
              onChange={(event) => void handleStatusChange(event.target.value as PlayStatus)}
            >
              {PLAY_STATUS_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          <label>
            Nota
            <select
              value={game.rating ?? ''}
              onChange={(event) => void handleRatingChange(event.target.value)}
            >
              <option value="">Sem nota</option>
              {Array.from({ length: 10 }, (_, index) => index + 1).map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>
        </div>
      </div>

      <div className="detail-section">
        <h2>Sessões</h2>

        <form className="detail-controls" onSubmit={handleCreateSession}>
          <label>
            Duração (min)
            <input
              type="number"
              value={durationMinutes}
              onChange={(event) => setDurationMinutes(event.target.value)}
            />
          </label>

          <label>
            Data (opcional)
            <input
              type="date"
              value={playedAt}
              onChange={(event) => setPlayedAt(event.target.value)}
            />
          </label>

          <button type="submit" className="btn">
            Registrar sessão
          </button>
        </form>

        {sessionValidationError && <ErrorMessage message={sessionValidationError} />}

        {sessions.length === 0 ? (
          <p className="search-empty">Nenhuma sessão registrada.</p>
        ) : (
          <ul className="session-list">
            {sessions.map((session) => (
              <li key={session.id} className="session-item">
                <span>
                  {formatMinutes(session.durationMinutes)} · {session.playedAt}
                </span>
                <button
                  type="button"
                  className="btn btn-secondary"
                  onClick={() => void handleDeleteSession(session.id)}
                >
                  Remover
                </button>
              </li>
            ))}
          </ul>
        )}
      </div>

      <div className="detail-section">
        <button type="button" className="btn btn-danger" onClick={() => void handleDeleteGame()}>
          Remover jogo
        </button>
      </div>
    </section>
  )
}
