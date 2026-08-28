import { type FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router'

import { ApiError } from '../api/apiClient'
import { createTrackedGame, listTrackedGames, searchGames } from '../api/gamesApi'
import type { GameSummaryDto } from '../api/types'
import { CoverImage, ErrorMessage, LoadingMessage } from '../components/Feedback'

export function SearchPage() {
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [exact, setExact] = useState(false)
  const [results, setResults] = useState<GameSummaryDto[]>([])
  const [loading, setLoading] = useState(false)
  const [searched, setSearched] = useState(false)
  const [validationError, setValidationError] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [conflictLink, setConflictLink] = useState<string | null>(null)
  const [trackingRawgId, setTrackingRawgId] = useState<number | null>(null)

  async function handleSearch(event: FormEvent) {
    event.preventDefault()

    const trimmed = query.trim()
    if (!trimmed) {
      setValidationError('Digite um termo de busca.')
      setError(null)
      setConflictLink(null)
      return
    }

    setValidationError(null)
    setError(null)
    setConflictLink(null)
    setLoading(true)
    setSearched(true)

    try {
      const data = await searchGames(trimmed, exact || undefined)
      setResults(data)
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Erro ao buscar jogos.'
      setError(message)
      setResults([])
    } finally {
      setLoading(false)
    }
  }

  async function handleTrack(rawgId: number) {
    if (trackingRawgId !== null) {
      return
    }

    setTrackingRawgId(rawgId)
    setError(null)
    setConflictLink(null)

    try {
      const game = await createTrackedGame(rawgId)
      navigate(`/games/${game.id}`)
    } catch (err) {
      if (err instanceof ApiError && err.status === 409) {
        setError('Este jogo já está na sua lista.')
        try {
          const tracked = await listTrackedGames()
          const existing = tracked.find((item) => item.rawgId === rawgId)
          setConflictLink(existing ? `/games/${existing.id}` : '/')
        } catch {
          setConflictLink('/')
        }
      } else {
        const message = err instanceof ApiError ? err.message : 'Erro ao acompanhar jogo.'
        setError(message)
      }
    } finally {
      setTrackingRawgId(null)
    }
  }

  return (
    <section>
      <h1>Buscar jogos</h1>

      <form className="search-form" onSubmit={handleSearch}>
        <label className="search-form__field">
          <span className="search-form__label">Nome do jogo</span>
          <input
            type="search"
            aria-label="Nome do jogo"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            disabled={loading}
          />
        </label>

        <label className="search-form__checkbox">
          <input
            type="checkbox"
            checked={exact}
            onChange={(event) => setExact(event.target.checked)}
            disabled={loading}
          />
          Busca exata
        </label>

        <button type="submit" className="btn" disabled={loading}>
          Buscar
        </button>
      </form>

      {validationError && <ErrorMessage message={validationError} />}

      {loading && <LoadingMessage message="Buscando jogos..." />}

      {error && (
        <div className="search-feedback">
          <ErrorMessage message={error} />
          {conflictLink && (
            <p>
              <Link to={conflictLink}>
                {conflictLink === '/' ? 'Ir para meus jogos' : 'Ver jogo na lista'}
              </Link>
            </p>
          )}
        </div>
      )}

      {!loading && searched && results.length === 0 && !error && (
        <p className="search-empty">Nenhum jogo encontrado.</p>
      )}

      {!loading && results.length > 0 && (
        <ul className="game-list">
          {results.map((game) => (
            <li key={game.rawgId} className="search-result">
              <CoverImage src={game.coverUrl} alt={game.name} />
              <div className="game-list__meta">
                <p className="game-list__name">{game.name}</p>
                <p className="game-list__details">
                  {game.year !== null ? game.year : 'Ano desconhecido'}
                </p>
              </div>
              <button
                type="button"
                className="btn"
                onClick={() => void handleTrack(game.rawgId)}
                disabled={trackingRawgId !== null}
              >
                {trackingRawgId === game.rawgId ? 'Acompanhando...' : 'Acompanhar'}
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
