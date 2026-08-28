import { describe, expect, it } from 'vitest'

import { formatMinutes } from './formatMinutes'

describe('formatMinutes', () => {
  it('Given zero minutes, When formatMinutes is called, Then it returns "0 min"', () => {
    expect(formatMinutes(0)).toBe('0 min')
  })

  it('Given minutes only, When formatMinutes is called, Then it returns minutes with "min"', () => {
    expect(formatMinutes(45)).toBe('45 min')
  })

  it('Given exact hours, When formatMinutes is called, Then it returns hours only', () => {
    expect(formatMinutes(60)).toBe('1h')
  })

  it('Given hours and minutes, When formatMinutes is called, Then it returns both parts', () => {
    expect(formatMinutes(90)).toBe('1h 30min')
  })
})
