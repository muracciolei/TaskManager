import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <Link to="/dashboard">Task Manager</Link>
      </div>
      <div className="navbar-menu">
        <span className="navbar-user">Welcome, {user?.name || 'User'}</span>
        <button onClick={handleLogout} className="btn btn-logout">
          Logout
        </button>
      </div>
    </nav>
  )
}

export default Navbar
