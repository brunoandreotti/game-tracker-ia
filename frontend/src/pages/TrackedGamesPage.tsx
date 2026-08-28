import { useEffect, useState } from 'react'
import { Link } from 'react-router'

import { ApiError } from '../api/apiClient'
import { listTrackedGames } from '../api/gamesApi'
import type { TrackedGameDto } from '../api/types'
import { CoverImage, ErrorMessage, LoadingMessage } from '../components/Feedback'
import { formatMinutes } from '../lib/formatMinutes'
import { playStatusLabel } from '../lib/playStatus'

function RatingStars({ rating }: { rating: number }) {
  const filled = '★'.repeat(rating)
  const empty = '☆'.repeat(5 - rating)

  return (
    <span className="rating-stars" aria-label={`Nota ${rating} de 5`}>
      {filled}
      {empty}
    </span>
  )
}

function ratingLabel(rating: number | null) {
  if (rating === null) {
    return 'Sem nota'
  }

  return <RatingStars rating={rating} />
}

export function TrackedGamesPage() {
  const [games, setGames] = useState<TrackedGameDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    async function loadGames() {
      setLoading(true)
      setError(null)

      try {
        const data = await listTrackedGames()
        if (!cancelled) {
          setGames(data)
        }
      } catch (err) {
        if (!cancelled) {
          const message =
            err instanceof ApiError ? err.message : 'Erro ao carregar jogos acompanhados.'
          setError(message)
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    void loadGames()

    return () => {
      cancelled = true
    }
  }, [])

  if (loading) {
    return <LoadingMessage message="Carregando jogos..." />
  }

  if (error) {
    return <ErrorMessage message={error} />
  }

  if (games.length === 0) {
    return (
      <div className="empty-state">
        <p>Você ainda não acompanha nenhum jogo.</p>
        <div className="empty-state__cta">
          <Link to="/search" className="btn">
            Buscar jogos
          </Link>
        </div>
      </div>
    )
  }

  return (
    <section>
      <h1>Meus jogos</h1>
      <ul className="game-list">
        {games.map((game) => (
          <li key={game.id}>
            <Link to={`/games/${game.id}`} className="game-list__item">
              <CoverImage src={game.coverUrl} alt={game.name} />
              <div className="game-list__meta">
                <p className="game-list__name">{game.name}</p>
                <p className="game-list__details">
                  {playStatusLabel(game.status)}
                  {' · '}
                  {ratingLabel(game.rating)}
                  {' · '}
                  {formatMinutes(game.totalMinutes)}
                </p>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  )
}
