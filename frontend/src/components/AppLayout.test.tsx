import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router'
import { describe, expect, it } from 'vitest'

import { AppLayout } from './AppLayout'

describe('AppLayout', () => {
  it('Given the app layout, When rendered, Then nav links point to list and search routes', () => {
    render(
      <MemoryRouter initialEntries={['/']}>
        <Routes>
          <Route element={<AppLayout />}>
            <Route path="/" element={<div>Home</div>} />
            <Route path="/search" element={<div>Search</div>} />
          </Route>
        </Routes>
      </MemoryRouter>,
    )

    expect(screen.getByRole('link', { name: 'Game Tracker' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('link', { name: 'Meus jogos' })).toHaveAttribute('href', '/')
    expect(screen.getByRole('link', { name: 'Buscar' })).toHaveAttribute('href', '/search')
  })
})
