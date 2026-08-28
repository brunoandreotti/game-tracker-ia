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
import { Button } from '../components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../components/ui/card'
import { Input } from '../components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select'
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

    if (ratingValue === '' || ratingValue === 'none') {
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
          <Button asChild>
            <Link to="/">Voltar para meus jogos</Link>
          </Button>
        </div>
      </div>
    )
  }

  if (error || !game) {
    return <ErrorMessage message={error ?? 'Erro ao carregar jogo.'} />
  }

  return (
    <section className="detail-page">
      <p className="detail-back">
        <Link to="/">← Voltar para meus jogos</Link>
      </p>

      <header className="detail-header">
        <CoverImage src={game.coverUrl} alt={game.name} width={96} height={134} />
        <div className="detail-meta">
          <h1>{game.name}</h1>
          <p className="detail-meta__stats">
            {game.year !== null ? game.year : 'Ano desconhecido'}
            {' · '}
            {playStatusLabel(game.status)}
            {' · '}
            {game.rating !== null ? `Nota ${game.rating}` : 'Sem nota'}
            {' · '}
            {formatMinutes(game.totalMinutes)}
          </p>
        </div>
      </header>

      {mutationError && <ErrorMessage message={mutationError} />}

      <Card>
        <CardHeader className="border-b">
          <CardTitle>Progresso</CardTitle>
          <CardDescription>Atualize status e nota deste jogo.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 sm:grid-cols-2">
            <div className="flex flex-col gap-1.5">
              <span className="text-muted-foreground text-xs font-bold tracking-wide uppercase">
                Status
              </span>
              <Select
                value={game.status}
                onValueChange={(value) => {
                  if (value) {
                    void handleStatusChange(value as PlayStatus)
                  }
                }}
              >
                <SelectTrigger aria-label="Status" className="w-full">
                  <SelectValue placeholder="Status" />
                </SelectTrigger>
                <SelectContent>
                  {PLAY_STATUS_OPTIONS.map((option) => (
                    <SelectItem key={option.value} value={option.value}>
                      {option.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="flex flex-col gap-1.5">
              <span className="text-muted-foreground text-xs font-bold tracking-wide uppercase">
                Nota
              </span>
              <Select
                value={game.rating !== null ? String(game.rating) : 'none'}
                onValueChange={(value) => {
                  if (value) {
                    void handleRatingChange(value)
                  }
                }}
              >
                <SelectTrigger aria-label="Nota" className="w-full">
                  <SelectValue placeholder="Sem nota" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">Sem nota</SelectItem>
                  {Array.from({ length: 10 }, (_, index) => index + 1).map((value) => (
                    <SelectItem key={value} value={String(value)}>
                      {value}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="border-b">
          <CardTitle>Sessões</CardTitle>
          <CardDescription>Registre quanto tempo jogou e quando.</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-6">
          <form
            className="grid items-end gap-4 sm:grid-cols-[1fr_1fr_auto]"
            onSubmit={handleCreateSession}
          >
            <div className="flex flex-col gap-1.5">
              <label
                htmlFor="session-duration"
                className="text-muted-foreground text-xs font-bold tracking-wide uppercase"
              >
                Duração (min)
              </label>
              <Input
                id="session-duration"
                type="number"
                value={durationMinutes}
                onChange={(event) => setDurationMinutes(event.target.value)}
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label
                htmlFor="session-date"
                className="text-muted-foreground text-xs font-bold tracking-wide uppercase"
              >
                Data (opcional)
              </label>
              <Input
                id="session-date"
                type="date"
                value={playedAt}
                onChange={(event) => setPlayedAt(event.target.value)}
              />
            </div>

            <Button type="submit">Registrar sessão</Button>
          </form>

          {sessionValidationError && <ErrorMessage message={sessionValidationError} />}

          {sessions.length === 0 ? (
            <p className="text-muted-foreground text-sm">Nenhuma sessão registrada.</p>
          ) : (
            <ul className="session-list">
              {sessions.map((session) => (
                <li key={session.id} className="session-item">
                  <span>
                    {formatMinutes(session.durationMinutes)} · {session.playedAt}
                  </span>
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() => void handleDeleteSession(session.id)}
                  >
                    Remover
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </CardContent>
      </Card>

      <div className="detail-danger">
        <Button type="button" variant="destructive" onClick={() => void handleDeleteGame()}>
          Remover jogo
        </Button>
      </div>
    </section>
  )
}
