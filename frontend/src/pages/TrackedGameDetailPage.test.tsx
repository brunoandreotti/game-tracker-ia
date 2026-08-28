import { cleanup, render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError } from '../api/apiClient'
import * as gamesApi from '../api/gamesApi'
import { TrackedGameDetailPage } from './TrackedGameDetailPage'

const navigateMock = vi.fn()

vi.mock('react-router', async () => {
  const actual = await vi.importActual<typeof import('react-router')>('react-router')
  return {
    ...actual,
    useNavigate: () => navigateMock,
  }
})

vi.mock('../api/gamesApi', () => ({
  getTrackedGame: vi.fn(),
  listSessions: vi.fn(),
  patchTrackedGame: vi.fn(),
  createSession: vi.fn(),
  deleteSession: vi.fn(),
  deleteTrackedGame: vi.fn(),
}))

const trackedGame = {
  id: 1,
  rawgId: 42,
  name: 'The Legend of Zelda',
  year: 2017,
  coverUrl: null,
  status: 'PLAYING' as const,
  rating: null,
  totalMinutes: 0,
}

function renderDetailPage() {
  return render(
    <MemoryRouter initialEntries={['/games/1']}>
      <Routes>
        <Route path="/games/:id" element={<TrackedGameDetailPage />} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('TrackedGameDetailPage', () => {
  afterEach(() => {
    cleanup()
    vi.clearAllMocks()
  })

  it('Given a loaded game with no sessions, When the page renders, Then it shows game details and empty sessions text', async () => {
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    expect(screen.getByRole('link', { name: '← Voltar para meus jogos' })).toHaveAttribute(
      'href',
      '/',
    )
    expect(screen.getByText(/2017 · Jogando · Sem nota · 0 min/)).toBeInTheDocument()
    expect(screen.getByText('Nenhuma sessão registrada.')).toBeInTheDocument()
  })

  it('Given a loaded game with coverUrl, When the page renders, Then it shows the cover image', async () => {
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue({
      ...trackedGame,
      coverUrl: 'https://example.com/cover.jpg',
    })
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('img', { name: 'The Legend of Zelda' })).toHaveAttribute(
        'src',
        'https://example.com/cover.jpg',
      )
    })
  })

  it('Given a loaded game with sessions, When the page renders, Then it shows session rows', async () => {
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue({ ...trackedGame, totalMinutes: 60 })
    vi.mocked(gamesApi.listSessions).mockResolvedValue([
      { id: 10, durationMinutes: 60, playedAt: '2024-06-01' },
    ])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText(/1h · 2024-06-01/)).toBeInTheDocument()
    })
  })

  it('Given status changed to COMPLETED, When the user selects Zerei, Then patchTrackedGame is called and UI updates', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])
    vi.mocked(gamesApi.patchTrackedGame).mockResolvedValue({
      ...trackedGame,
      status: 'COMPLETED',
    })

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('combobox', { name: 'Status' }))
    await user.click(await screen.findByRole('option', { name: 'Zerei' }))

    await waitFor(() => {
      expect(gamesApi.patchTrackedGame).toHaveBeenCalledWith(1, { status: 'COMPLETED' })
      expect(screen.getByText(/2017 · Zerei · Sem nota · 0 min/)).toBeInTheDocument()
    })
  })

  it('Given rating set to 5, When the user selects 5, Then patchTrackedGame is called and UI shows Nota 5', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])
    vi.mocked(gamesApi.patchTrackedGame).mockResolvedValue({
      ...trackedGame,
      rating: 5,
    })

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('combobox', { name: 'Nota' }))
    await user.click(await screen.findByRole('option', { name: '5' }))

    await waitFor(() => {
      expect(gamesApi.patchTrackedGame).toHaveBeenCalledWith(1, { rating: 5 })
      expect(screen.getByText(/2017 · Jogando · Nota 5 · 0 min/)).toBeInTheDocument()
    })
  })

  it('Given rating set to 0, When the user selects 0, Then patchTrackedGame is called and UI shows Nota 0', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])
    vi.mocked(gamesApi.patchTrackedGame).mockResolvedValue({
      ...trackedGame,
      rating: 0,
    })

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('combobox', { name: 'Nota' }))
    await user.click(await screen.findByRole('option', { name: '0' }))

    await waitFor(() => {
      expect(gamesApi.patchTrackedGame).toHaveBeenCalledWith(1, { rating: 0 })
      expect(screen.getByText(/2017 · Jogando · Nota 0 · 0 min/)).toBeInTheDocument()
    })
  })

  it('Given rated game, When the user selects Sem nota, Then patchTrackedGame is not called', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue({ ...trackedGame, rating: 5 })
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('combobox', { name: 'Nota' }))
    await user.click(await screen.findByRole('option', { name: 'Sem nota' }))

    expect(gamesApi.patchTrackedGame).not.toHaveBeenCalled()
  })

  it('Given valid session duration, When the user submits, Then createSession is called and totals refresh', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame)
      .mockResolvedValueOnce(trackedGame)
      .mockResolvedValueOnce({ ...trackedGame, totalMinutes: 90 })
    vi.mocked(gamesApi.listSessions)
      .mockResolvedValueOnce([])
      .mockResolvedValueOnce([{ id: 5, durationMinutes: 90, playedAt: '2024-01-01' }])
    vi.mocked(gamesApi.createSession).mockResolvedValue({
      id: 5,
      durationMinutes: 90,
      playedAt: '2024-01-01',
    })

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText('Duração (min)'), '90')
    await user.click(screen.getByRole('button', { name: 'Registrar sessão' }))

    await waitFor(() => {
      expect(gamesApi.createSession).toHaveBeenCalledWith(1, { durationMinutes: 90 })
      expect(screen.getByText(/2017 · Jogando · Sem nota · 1h 30min/)).toBeInTheDocument()
    })
  })

  it('Given confirm declined, When the user clicks Remover on a session, Then deleteSession is not called', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([
      { id: 10, durationMinutes: 30, playedAt: '2024-06-01' },
    ])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Remover' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Remover' }))

    expect(await screen.findByRole('alertdialog')).toBeInTheDocument()
    expect(screen.getByText('Remover esta sessão?')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Cancelar' }))

    await waitFor(() => {
      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    })
    expect(gamesApi.deleteSession).not.toHaveBeenCalled()
  })

  it('Given confirm accepted, When the user clicks Remover on a session, Then deleteSession is called and sessions and total refresh', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame)
      .mockResolvedValueOnce({ ...trackedGame, totalMinutes: 30 })
      .mockResolvedValueOnce({ ...trackedGame, totalMinutes: 0 })
    vi.mocked(gamesApi.listSessions)
      .mockResolvedValueOnce([{ id: 10, durationMinutes: 30, playedAt: '2024-06-01' }])
      .mockResolvedValueOnce([])
    vi.mocked(gamesApi.deleteSession).mockResolvedValue(undefined)

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText(/30 min · 2024-06-01/)).toBeInTheDocument()
      expect(screen.getByText(/2017 · Jogando · Sem nota · 30 min/)).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Remover' }))
    await user.click(await screen.findByRole('button', { name: 'Confirmar' }))

    await waitFor(() => {
      expect(gamesApi.deleteSession).toHaveBeenCalledWith(1, 10)
      expect(screen.getByText('Nenhuma sessão registrada.')).toBeInTheDocument()
      expect(screen.getByText(/2017 · Jogando · Sem nota · 0 min/)).toBeInTheDocument()
    })
  })

  it('Given confirm accepted, When the user removes the game, Then deleteTrackedGame is called and navigates home', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])
    vi.mocked(gamesApi.deleteTrackedGame).mockResolvedValue(undefined)

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Remover jogo' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Remover jogo' }))
    expect(await screen.findByText('Remover este jogo da sua lista?')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Confirmar' }))

    await waitFor(() => {
      expect(gamesApi.deleteTrackedGame).toHaveBeenCalledWith(1)
      expect(navigateMock).toHaveBeenCalledWith('/')
    })
  })

  it('Given game delete dialog open, When the user cancels, Then deleteTrackedGame is not called', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Remover jogo' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('button', { name: 'Remover jogo' }))
    await user.click(await screen.findByRole('button', { name: 'Cancelar' }))

    await waitFor(() => {
      expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument()
    })
    expect(gamesApi.deleteTrackedGame).not.toHaveBeenCalled()
  })

  it('Given patchTrackedGame fails, When the user changes status, Then error is shown and game name remains', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])
    vi.mocked(gamesApi.patchTrackedGame).mockRejectedValue(
      new ApiError(500, 'Falha ao atualizar status'),
    )

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.click(screen.getByRole('combobox', { name: 'Status' }))
    await user.click(await screen.findByRole('option', { name: 'Zerei' }))

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent('Falha ao atualizar status')
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })
  })

  it('Given invalid session duration, When the user submits, Then it does not call the API', async () => {
    const user = userEvent.setup()
    vi.mocked(gamesApi.getTrackedGame).mockResolvedValue(trackedGame)
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'The Legend of Zelda' })).toBeInTheDocument()
    })

    await user.type(screen.getByLabelText('Duração (min)'), '0')
    await user.click(screen.getByRole('button', { name: 'Registrar sessão' }))

    expect(gamesApi.createSession).not.toHaveBeenCalled()
    expect(screen.getByRole('alert')).toHaveTextContent(
      'Informe uma duração em minutos maior que zero.',
    )
  })

  it('Given a 404 from the API, When the page loads, Then it shows not found with link home', async () => {
    vi.mocked(gamesApi.getTrackedGame).mockRejectedValue(new ApiError(404, 'Não encontrado'))
    vi.mocked(gamesApi.listSessions).mockResolvedValue([])

    renderDetailPage()

    await waitFor(() => {
      expect(screen.getByText('Jogo não encontrado.')).toBeInTheDocument()
    })

    expect(screen.getByRole('link', { name: 'Voltar para meus jogos' })).toHaveAttribute('href', '/')
  })
})
