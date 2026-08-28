import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router'
import { describe, expect, it, vi } from 'vitest'

import * as gamesApi from '../api/gamesApi'
import { TrackedGamesPage } from './TrackedGamesPage'

vi.mock('../api/gamesApi', () => ({
  listTrackedGames: vi.fn(),
}))

describe('TrackedGamesPage', () => {
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
})
