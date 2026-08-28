import { afterEach, describe, expect, it, vi } from 'vitest'

import { ApiError, apiRequest } from './apiClient'

const baseUrl = 'http://localhost:8080'

describe('apiRequest', () => {
  afterEach(() => {
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('Given a successful JSON response, When apiRequest is called, Then it returns parsed data', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: vi.fn().mockResolvedValue({ id: 1, name: 'Zelda' }),
      }),
    )

    const result = await apiRequest<{ id: number; name: string }>('/tracked-games/1')

    expect(fetch).toHaveBeenCalledWith(`${baseUrl}/tracked-games/1`, undefined)
    expect(result).toEqual({ id: 1, name: 'Zelda' })
  })

  it('Given a non-OK response with error body, When apiRequest is called, Then it throws ApiError with message from body', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue({
        ok: false,
        status: 400,
        statusText: 'Bad Request',
        json: vi.fn().mockResolvedValue({
          status: 400,
          error: 'Bad Request',
          message: 'Parâmetro q é obrigatório',
        }),
      }),
    )

    await expect(apiRequest('/games/search?q=')).rejects.toMatchObject({
      status: 400,
      message: 'Parâmetro q é obrigatório',
    })
    await expect(apiRequest('/games/search?q=')).rejects.toBeInstanceOf(ApiError)
  })

  it('Given a network failure, When apiRequest is called, Then it throws ApiError with Portuguese unreachable message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(apiRequest('/tracked-games')).rejects.toMatchObject({
      status: 0,
      message: 'Não foi possível alcançar a API.',
    })
  })
})
