import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'

import { CoverImage } from './Feedback'

describe('CoverImage', () => {
  it('Given null src, When rendered, Then no image is shown', () => {
    render(<CoverImage src={null} alt="Cover" />)

    expect(screen.queryByRole('img')).not.toBeInTheDocument()
  })

  it('Given a cover URL, When rendered, Then image is shown with alt text', () => {
    render(<CoverImage src="https://example.com/cover.jpg" alt="Game Cover" />)

    expect(screen.getByRole('img', { name: 'Game Cover' })).toHaveAttribute(
      'src',
      'https://example.com/cover.jpg',
    )
  })
})
