export const RATING_MIN = 0
export const RATING_MAX = 5

/** Integer options for the detail Nota Select (excludes Sem nota / null). */
export const RATING_VALUES = [0, 1, 2, 3, 4, 5] as const

export function isValidRating(rating: number): boolean {
  return Number.isInteger(rating) && rating >= RATING_MIN && rating <= RATING_MAX
}

export function ratingValidationMessage(): string {
  return 'A nota deve ser um número entre 0 e 5.'
}
