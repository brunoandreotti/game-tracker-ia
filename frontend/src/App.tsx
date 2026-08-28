import { BrowserRouter, Route, Routes } from 'react-router'

import { AppLayout } from './components/AppLayout'
import { SearchPage } from './pages/SearchPage'
import { TrackedGameDetailPage } from './pages/TrackedGameDetailPage'
import { TrackedGamesPage } from './pages/TrackedGamesPage'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<AppLayout />}>
          <Route index element={<TrackedGamesPage />} />
          <Route path="search" element={<SearchPage />} />
          <Route path="games/:id" element={<TrackedGameDetailPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
