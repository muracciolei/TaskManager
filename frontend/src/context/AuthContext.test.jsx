import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, waitFor, fireEvent } from '@testing-library/react'
import React from 'react'
import { AuthProvider, AuthContext } from './AuthContext'

const TestConsumer = () => {
  const { user, isAuthenticated, login, logout, loading } = React.useContext(AuthContext)
  
  return (
    <div>
      <span data-testid="loading">{loading.toString()}</span>
      <span data-testid="authenticated">{isAuthenticated.toString()}</span>
      <span data-testid="user">{user ? user.email : 'no-user'}</span>
      <button onClick={() => login('test-token', { email: 'test@example.com' })} data-testid="login-btn">Login</button>
      <button onClick={logout} data-testid="logout-btn">Logout</button>
    </div>
  )
}

const renderWithContext = (component) => {
  return render(
    <AuthProvider>
      {component}
    </AuthProvider>
  )
}

describe('AuthProvider', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('should provide initial state with loading true', () => {
    renderWithContext(<TestConsumer />)
    expect(screen.getByTestId('loading').textContent).toBe('true')
  })

  it('should set loading to false after initialization', async () => {
    renderWithContext(<TestConsumer />)
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false')
    })
  })

  it('should not be authenticated initially', async () => {
    renderWithContext(<TestConsumer />)
    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('false')
    })
  })

  it('should not have user initially', async () => {
    renderWithContext(<TestConsumer />)
    await waitFor(() => {
      expect(screen.getByTestId('user').textContent).toBe('no-user')
    })
  })

  it('should authenticate user after login', async () => {
    renderWithContext(<TestConsumer />)
    
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false')
    })
    
    const loginButton = screen.getByTestId('login-btn')
    fireEvent.click(loginButton)
    
    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('true')
      expect(screen.getByTestId('user').textContent).toBe('test@example.com')
    })
  })

  it('should store token in localStorage after login', async () => {
    renderWithContext(<TestConsumer />)
    
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false')
    })
    
    const loginButton = screen.getByTestId('login-btn')
    fireEvent.click(loginButton)
    
    await waitFor(() => {
      expect(localStorage.setItem).toHaveBeenCalledWith('token', 'test-token')
    })
  })

  it('should logout user and clear localStorage', async () => {
    renderWithContext(<TestConsumer />)
    
    await waitFor(() => {
      expect(screen.getByTestId('loading').textContent).toBe('false')
    })
    
    // Login first
    const loginButton = screen.getByTestId('login-btn')
    fireEvent.click(loginButton)
    
    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('true')
    })
    
    // Then logout
    const logoutButton = screen.getByTestId('logout-btn')
    fireEvent.click(logoutButton)
    
    await waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('false')
      expect(screen.getByTestId('user').textContent).toBe('no-user')
    })
  })

  it('should load user from localStorage on mount', () => {
    localStorage.setItem('token', 'existing-token')
    localStorage.setItem('user', JSON.stringify({ email: 'saved@example.com' }))
    
    renderWithContext(<TestConsumer />)
    
    // Should immediately load user from localStorage
    waitFor(() => {
      expect(screen.getByTestId('authenticated').textContent).toBe('true')
      expect(screen.getByTestId('user').textContent).toBe('saved@example.com')
    })
  })
})
