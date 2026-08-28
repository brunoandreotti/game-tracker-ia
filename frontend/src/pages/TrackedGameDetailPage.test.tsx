import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it, vi } from 'vitest'

import { ApiError } from '../api/apiClient'
import * as gamesApi from '../api/gamesApi'
import { TrackedGameDetailPage } from './TrackedGameDetailPage'

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
