import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '../api/apiClient'
import * as gamesApi from '../api/gamesApi'
import type { GameSummaryDto } from '../api/types'
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

  it('Given search results with coverUrl, When the search completes, Then it shows name, year, and cover image', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([
      {
        rawgId: 1,
        name: 'Hollow Knight',
        year: 2017,
        coverUrl: 'https://example.com/cover.jpg',
      },
    ])

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Hollow')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('Hollow Knight')).toBeInTheDocument()
    })

    expect(screen.getByText('2017')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'Hollow Knight' })).toHaveAttribute(
      'src',
      'https://example.com/cover.jpg',
    )
  })

  it('Given an empty results array after search, When the search completes, Then it shows empty message', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([])

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'xyz')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByText('Nenhum jogo encontrado.')).toBeInTheDocument()
    })
  })

  it('Given searchGames rejects with ApiError, When the user searches, Then it shows the API error message', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockRejectedValue(new ApiError(502, 'RAWG indisponível'))

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Zelda')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('RAWG indisponível')
    })
  })

  it('Given a pending searchGames call, When the user searches, Then it shows loading and disables Buscar', async () => {
    const user = userEvent.setup()
    let resolveSearch!: (value: GameSummaryDto[]) => void
    vi.mocked(gamesApi.searchGames).mockReturnValue(
      new Promise((resolve) => {
        resolveSearch = resolve
      }),
    )

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Zelda')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    expect(screen.getByText('Buscando jogos...')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Buscar' })).toBeDisabled()

    resolveSearch([])
    await waitFor(() => {
      expect(screen.queryByText('Buscando jogos...')).not.toBeInTheDocument()
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

  it('Given tracking is in progress, When another result is shown, Then Acompanhar is disabled and shows Acompanhando...', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([
      {
        rawgId: 1,
        name: 'Game A',
        year: 2020,
        coverUrl: null,
      },
      {
        rawgId: 2,
        name: 'Game B',
        year: 2021,
        coverUrl: null,
      },
    ])
    vi.mocked(gamesApi.createTrackedGame).mockReturnValue(new Promise(() => {}))

    render(
      <MemoryRouter>
        <SearchPage />
      </MemoryRouter>,
    )

    await user.type(screen.getByLabelText('Nome do jogo'), 'Game')
    await user.click(screen.getByRole('button', { name: 'Buscar' }))

    await waitFor(() => {
      expect(screen.getAllByRole('button', { name: 'Acompanhar' })).toHaveLength(2)
    })

    await user.click(screen.getAllByRole('button', { name: 'Acompanhar' })[0]!)

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Acompanhando...' })).toBeInTheDocument()
    })

    expect(screen.getAllByRole('button', { name: 'Acompanhar' })[0]).toBeDisabled()
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

  it('Given a 409 on track, When list has no matching rawgId, Then it links to home', async () => {
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
    vi.mocked(gamesApi.listTrackedGames).mockResolvedValue([])

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
      expect(screen.getByRole('link', { name: 'Ir para meus jogos' })).toHaveAttribute('href', '/')
    })
  })

  it('Given createTrackedGame fails with non-409, When the user tracks, Then it shows the API error message', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.searchGames).mockResolvedValue([
      {
        rawgId: 42,
        name: 'The Legend of Zelda',
        year: 2017,
        coverUrl: null,
      },
    ])
    vi.mocked(gamesApi.createTrackedGame).mockRejectedValue(new ApiError(500, 'Falha no servidor'))

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
      expect(screen.getByRole('alert')).toHaveTextContent('Falha no servidor')
    })
  })
})
