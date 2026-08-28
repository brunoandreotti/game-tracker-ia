import { afterEach, describe, expect, it, vi } from 'vitest'

import { createTrackedGame, searchGames } from './gamesApi'

const baseUrl = 'http://localhost:8080'

describe('gamesApi', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('Given a search query with exact flag, When searchGames is called, Then it requests the correct query params', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: vi.fn().mockResolvedValue([]),
      }),
    )

    await searchGames('Lies Of P', true)

    expect(fetch).toHaveBeenCalledWith(
      `${baseUrl}/games/search?q=Lies+Of+P&exact=true`,
      undefined,
    )
  })

  it('Given a rawgId, When createTrackedGame is called, Then it POSTs only rawgId in the body', async () => {
    const trackedGame = {
      id: 1,
      rawgId: 123,
      name: 'Zelda',
      year: 2017,
      coverUrl: null,
      status: 'PLAYING' as const,
      rating: null,
      totalMinutes: 0,
    }

    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 201,
        json: vi.fn().mockResolvedValue(trackedGame),
      }),
    )

    const result = await createTrackedGame(123)

    expect(fetch).toHaveBeenCalledWith(`${baseUrl}/tracked-games`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rawgId: 123 }),
    })
    expect(result).toEqual(trackedGame)
  })
})
