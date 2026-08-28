import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '../api/apiClient'
import * as gamesApi from '../api/gamesApi'
import { SearchPage } from './SearchPage'

const navigateMock = vi.fn()

vi.mock('react-router', async () => {
  const actual = await vi.importActual<typeof import('react-router')>('react-router')
  return {
    ...actual,
    useNavigate: () => navigateMock,
  }
})

vi.mock('../api/gamesApi', () => ({
  searchGames: vi.fn(),
  createTrackedGame: vi.fn(),
  listTrackedGames: vi.fn(),
}))

describe('SearchPage', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('Given an empty query, When the user submits search, Then it does not call the API', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(gamesApi.searchGames).not.toHaveBeenCalled()
    expect(screen.getByRole('alert')).toHaveTextContent('Digite um termo de busca.')
  })

  it('Given whitespace-only query, When the user submits search, Then it does not call the API', async () => {
    const user = userEvent.setup()

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), '   ')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(gamesApi.searchGames).not.toHaveBeenCalled()
    expect(screen.getByRole('alert')).toHaveTextContent('Digite um termo de busca.')
  })

  it('Given exact search enabled, When the user submits, Then searchGames is called with exact=true', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([])

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Zelda')
    await user.click(screen.getByRole('checkbox', { name: 'Busca exata' }))
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(gamesApi.searchGames).toHaveBeenCalledWith('Zelda', true)
    })
  })

  it('Given a search result, When the user tracks it, Then it navigates to the game detail', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([
      {
        rawgId: 42,
        name: 'The Legend of Zelda',
        year: 2017,
        coverUrl: null,
      },
    ])
    vi.mocked(gamesApi.createTrackedGame).mockResolvedValue({
      id: 7,
      rawgId: 42,
      name: 'The Legend of Zelda',
      year: 2017,
      coverUrl: null,
      status: 'PLAYING',
      rating: null,
      totalMinutes: 0,
    })

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Zelda')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Acompanhar' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Acompanhar' }))

    await waitFor(() => {
      expect(gamesApi.createTrackedGame).toHaveBeenCalledWith(42)
      expect(navigateMock).toHaveBeenCalledWith('/games/7')
    })
  })

  it('Given a 409 on track, When list has the game, Then it shows a link to the existing entry', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([
      {
        rawgId: 42,
        name: 'The Legend of Zelda',
        year: 2017,
        coverUrl: null,
      },
    ])
    vi.mocked(gamesApi.createTrackedGame).mockRejectedValue(
      new ApiError(409, 'Jogo já acompanhado'),
    )
    vi.mocked(gamesApi.listTrackedGames).mockResolvedValue([
      {
        id: 3,
        rawgId: 42,
        name: 'The Legend of Zelda',
        year: 2017,
        coverUrl: null,
        status: 'PLAYING',
        rating: null,
        totalMinutes: 0,
      },
    ])

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Zelda')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Acompanhar' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Acompanhar' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Este jogo já está na sua lista.')
      expect(screen.getByRole('link', { name: 'Ver jogo na lista' })).toHaveAttribute(
        'href',
        '/games/3',
      )
    })
  })
})
