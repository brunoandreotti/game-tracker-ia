import { describe, expect, it } from 'vitest'

import { PLAY_STATUS_OPTIONS, playStatusLabel } from './playStatus'

describe('playStatusLabel', () => {
  it('Given WANT_TO_PLAY, When playStatusLabel is called, Then it returns "Quero jogar"', () => {
    expect(playStatusLabel('WANT_TO_PLAY')).toBe('Quero jogar')
  })

  it('Given PLAYING, When playStatusLabel is called, Then it returns "Jogando"', () => {
    expect(playStatusLabel('PLAYING')).toBe('Jogando')
  })

  it('Given COMPLETED, When playStatusLabel is called, Then it returns "Zerei"', () => {
    expect(playStatusLabel('COMPLETED')).toBe('Zerei')
  })

  it('Given DROPPED, When playStatusLabel is called, Then it returns "Dropado"', () => {
    expect(playStatusLabel('DROPPED')).toBe('Dropado')
  })
})

describe('PLAY_STATUS_OPTIONS', () => {
  it('Given PLAY_STATUS_OPTIONS, When inspected, Then it contains all four statuses with labels', () => {
    expect(PLAY_STATUS_OPTIONS).toHaveLength(4)
    expect(PLAY_STATUS_OPTIONS).toEqual(
      expect.arrayContaining([
        { value: 'WANT_TO_PLAY', label: 'Quero jogar' },
        { value: 'PLAYING', label: 'Jogando' },
        { value: 'COMPLETED', label: 'Zerei' },
        { value: 'DROPPED', label: 'Dropado' },
      ]),
    )
  })
})
