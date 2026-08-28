import type { ApiErrorBody } from './types'

export class ApiError extends Error {
  readonly status: number
  readonly body?: ApiErrorBody

  constructor(status: number, message: string, body?: ApiErrorBody) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.body = body
  }
}

const NETWORK_ERROR_MESSAGE = 'Não foi possível alcançar a API.'

function getBaseUrl(): string {
  return import.meta.env.VITE_API_URL
}

export async function apiRequest<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${getBaseUrl()}${path}`

  let response: Response
  try {
    response = await fetch(url, init)
  } catch {
    throw new ApiError(0, NETWORK_ERROR_MESSAGE)
  }

  if (response.ok) {
    if (response.status === 204) {
      return undefined as T
    }
    return (await response.json()) as T
  }

  let message = response.statusText || 'Erro na requisição.'
  let body: ApiErrorBody | undefined

  try {
    const errorBody = (await response.json()) as ApiErrorBody
    if (errorBody.message) {
      message = errorBody.message
    }
    body = errorBody
  } catch {
    // response body is not JSON
  }

  throw new ApiError(response.status, message, body)
}
