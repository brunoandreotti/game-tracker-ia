import { cleanup, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '../api/apiClient'
import * as gamesApi from '../api/gamesApi'
import type { TrackedGameDto } from '../api/types'
import { TrackedGamesPage } from './TrackedGamesPage'

vi.mock('../api/gamesApi', () => ({
  listTrackedGames: vi.fn(),
}))

describe('TrackedGamesPage', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('Given an empty tracked games list, When the page loads, Then it shows empty state with link to search', async () => {
    vi.mocked(gamesApi.listTrackedGames).mockResolvedValue([])

    render(
      <MemoryRouter>
        <TrackedGamesPage />
      </MemoryRouter>,
    )

    await waitFor(() => {
      expect(screen.getByText('Você ainda não acompanha nenhum jogo.')).toBeInTheDocument()
    })

    expect(screen.getByRole('link', { name: 'Buscar jogos' })).toHaveAttribute('href', '/search')
  })

  it('Given two tracked games, When the page loads, Then it shows names, PT status, stars or Sem nota, minutes, and detail links', async () => {
    vi.mocked(gamesApi.listTrackedGames).mockResolvedValue([
      {
        id: 1,
        rawgId: 10,
        name: 'Game One',
        year: 2020,
        coverUrl: null,
        status: 'PLAYING',
        rating: null,
        totalMinutes: 45,
      },
      {
        id: 2,
        rawgId: 20,
        name: 'Game Two',
        year: 2021,
        coverUrl: null,
        status: 'COMPLETED',
        rating: 5,
        totalMinutes: 120,
      },
      {
        id: 3,
        rawgId: 30,
        name: 'Game Three',
        year: 2022,
        coverUrl: null,
        status: 'DROPPED',
        rating: 0,
        totalMinutes: 10,
      },
    ])

    render(
      <MemoryRouter>
        <TrackedGamesPage />
      </MemoryRouter>,
    )

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Meus jogos' })).toBeInTheDocument()
    })

    expect(screen.getByText('Game One')).toBeInTheDocument()
    expect(screen.getByText('Game Two')).toBeInTheDocument()
    expect(screen.getByText('Game Three')).toBeInTheDocument()
    expect(screen.getByText(/Jogando/)).toBeInTheDocument()
    expect(screen.getByText(/Zerei/)).toBeInTheDocument()
    expect(screen.getByText(/Dropado/)).toBeInTheDocument()
    expect(screen.getByText(/Sem nota/)).toBeInTheDocument()
    expect(screen.getByLabelText('Nota 5 de 5')).toHaveTextContent('★★★★★')
    expect(screen.getByLabelText('Nota 0 de 5')).toHaveTextContent('☆☆☆☆☆')
    expect(screen.queryByText(/Nota 5/)).not.toBeInTheDocument()
    expect(screen.getByText(/45 min/)).toBeInTheDocument()
    expect(screen.getByText(/2h/)).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Game One/ })).toHaveAttribute('href', '/games/1')
    expect(screen.getByRole('link', { name: /Game Two/ })).toHaveAttribute('href', '/games/2')
    expect(gamesApi.listTrackedGames).toHaveBeenCalled()
  })

  it('Given listTrackedGames rejects with ApiError, When the page loads, Then it shows the error and no list', async () => {
    vi.mocked(gamesApi.listTrackedGames).mockRejectedValue(
      new ApiError(500, 'Servidor indisponível'),
    )

    render(
      <MemoryRouter>
        <TrackedGamesPage />
      </MemoryRouter>,
    )

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Servidor indisponível')
    })

    expect(screen.queryByRole('heading', { name: 'Meus jogos' })).not.toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Game/ })).not.toBeInTheDocument()
  })

  it('Given a pending listTrackedGames call, When the page loads, Then it shows loading message', async () => {
    let resolveList!: (value: TrackedGameDto[]) => void
    vi.mocked(gamesApi.listTrackedGames).mockReturnValue(
      new Promise((resolve) => {
        resolveList = resolve
      }),
    )

    render(
      <MemoryRouter>
        <TrackedGamesPage />
      </MemoryRouter>,
    )

    expect(screen.getByText('Carregando jogos...')).toBeInTheDocument()

    resolveList([])
    await waitFor(() => {
      expect(screen.queryByText('Carregando jogos...')).not.toBeInTheDocument()
    })
  })
})
