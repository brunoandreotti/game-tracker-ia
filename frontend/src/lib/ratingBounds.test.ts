import { describe, expect, it } from 'vitest'

import { isValidRating, RATING_VALUES, ratingValidationMessage } from './ratingBounds'

describe('RATING_VALUES', () => {
  it('Given the rating scale, When RATING_VALUES is read, Then it is exactly 0 through 5', () => {
    expect([...RATING_VALUES]).toEqual([0, 1, 2, 3, 4, 5])
  })
})

describe('isValidRating', () => {
  it('Given integers 0 through 5, When isValidRating is called, Then it returns true', () => {
    for (const rating of [0, 1, 2, 3, 4, 5]) {
      expect(isValidRating(rating)).toBe(true)
    }
  })

  it('Given values outside 0-5, When isValidRating is called, Then it returns false', () => {
    expect(isValidRating(-1)).toBe(false)
    expect(isValidRating(6)).toBe(false)
    expect(isValidRating(10)).toBe(false)
    expect(isValidRating(Number.NaN)).toBe(false)
    expect(isValidRating(3.5)).toBe(false)
  })
})

describe('ratingValidationMessage', () => {
  it('Given an invalid rating, When the validation message is read, Then it mentions 0 and 5 in Portuguese', () => {
    const message = ratingValidationMessage()

    expect(message).toMatch(/0/)
    expect(message).toMatch(/5/)
    expect(message).toContain('A nota deve ser um número entre 0 e 5.')
  })
})
