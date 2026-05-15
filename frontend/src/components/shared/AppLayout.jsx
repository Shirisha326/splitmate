import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { LayoutDashboard, Users, LogOut, Moon, Sun } from 'lucide-react'
import Avatar from './Avatar'
import { useState, useEffect } from 'react'

export default function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [darkMode, setDarkMode] = useState(() => {
    return localStorage.getItem('splitmate_dark') === 'true'
  })

  useEffect(() => {
    if (darkMode) {
      document.body.classList.add('dark')
    } else {
      document.body.classList.remove('dark')
    }
    localStorage.setItem('splitmate_dark', darkMode)
  }, [darkMode])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-layout">

      {/* Mobile Header */}
      <div className="mobile-header">
        <h1>Split<span>Mate</span></h1>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          <button className="dark-toggle" onClick={() => setDarkMode(!darkMode)}>
            {darkMode
              ? <Sun size={18} color="#faf8f3" />
              : <Moon size={18} color="#c2bdb8" />
            }
          </button>
          <button
            style={{ background: 'none', border: 'none', cursor: 'pointer' }}
            onClick={handleLogout}
          >
            <LogOut size={18} color="rgba(250,248,243,0.6)" />
          </button>
        </div>
      </div>

      {/* Desktop Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-logo">
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <h1>Split<span>Mate</span></h1>
            <button
              className="dark-toggle"
              onClick={() => setDarkMode(!darkMode)}
              title={darkMode ? 'Light mode' : 'Dark mode'}
            >
              {darkMode
                ? <Sun size={18} color="#faf8f3" />
                : <Moon size={18} color="#c2bdb8" />
              }
            </button>
          </div>
          <p>Split expenses, not friendships</p>
        </div>

        <nav className="sidebar-nav">
          <div className="nav-section-label">Navigation</div>
          <NavLink
            to="/dashboard"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            <LayoutDashboard size={17} />
            Dashboard
          </NavLink>
          <NavLink
            to="/groups"
            className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`}
          >
            <Users size={17} />
            My Groups
          </NavLink>
        </nav>

        <div className="sidebar-user">
          <Avatar name={user?.name} color={user?.avatarColor} size="md" />
          <div style={{ flex: 1, minWidth: 0 }}>
            <div className="sidebar-user-name">{user?.name}</div>
            <div className="sidebar-user-email">{user?.email}</div>
          </div>
          <button
            className="btn btn-ghost btn-sm"
            onClick={handleLogout}
            title="Logout"
            style={{ padding: '6px', border: 'none',
              background: 'rgba(255,255,255,0.07)' }}
          >
            <LogOut size={16} color="rgba(250,248,243,0.6)" />
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        <Outlet />
      </main>

      {/* Mobile Bottom Navigation */}
      <nav className="mobile-nav">
        <div className="mobile-nav-inner">
          <NavLink
            to="/dashboard"
            className={({ isActive }) => `mobile-nav-btn${isActive ? ' active' : ''}`}
          >
            <LayoutDashboard size={22} />
            <span>Dashboard</span>
          </NavLink>
          <NavLink
            to="/groups"
            className={({ isActive }) => `mobile-nav-btn${isActive ? ' active' : ''}`}
          >
            <Users size={22} />
            <span>Groups</span>
          </NavLink>
          <button
            className="mobile-nav-btn"
            onClick={() => setDarkMode(!darkMode)}
          >
            {darkMode ? <Sun size={22} /> : <Moon size={22} />}
            <span>{darkMode ? 'Light' : 'Dark'}</span>
          </button>
          <button
            className="mobile-nav-btn"
            onClick={handleLogout}
          >
            <LogOut size={22} />
            <span>Logout</span>
          </button>
        </div>
      </nav>

    </div>
  )
}