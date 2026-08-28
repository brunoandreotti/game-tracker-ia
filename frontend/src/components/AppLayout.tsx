import { Link, Outlet, useLocation } from 'react-router'

export function AppLayout() {
  const { pathname } = useLocation()

  return (
    <div className="app-shell">
      <nav className="app-nav">
        <div className="app-nav__inner">
          <Link
            to="/"
            className={`app-nav__link${pathname === '/' ? ' app-nav__link--active' : ''}`}
          >
            Meus jogos
          </Link>
          <Link
            to="/search"
            className={`app-nav__link${pathname === '/search' ? ' app-nav__link--active' : ''}`}
          >
            Buscar
          </Link>
        </div>
      </nav>
      <main className="app-main">
        <Outlet />
      </main>
    </div>
  )
}
