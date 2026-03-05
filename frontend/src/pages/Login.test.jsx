import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Login from './Login'
import { AuthProvider } from '../context/AuthContext'
import * as api from '../services/api'

// Mock the API
vi.mock('../services/api', () => ({
  authAPI: {
    login: vi.fn()
  }
}))

// Mock useNavigate
const mockNavigate = vi.fn()
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom')
  return {
    ...actual,
    useNavigate: () => mockNavigate
  }
})

const renderWithRouter = (component) => {
  return render(
    <BrowserRouter>
      <AuthProvider>
        {component}
      </AuthProvider>
    </BrowserRouter>
  )
}

describe('Login', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    mockNavigate.mockClear()
  })

  it('should render login form', () => {
    renderWithRouter(<Login />)
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Login' })).toBeInTheDocument()
  })

  it('should show error when fields are empty', async () => {
    renderWithRouter(<Login />)
    
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))
    
    await waitFor(() => {
      expect(screen.getByText('Please fill in all fields')).toBeInTheDocument()
    })
  })

  it('should call login API with correct data', async () => {
    const mockResponse = { data: { token: 'test-token' } }
    api.authAPI.login.mockResolvedValue(mockResponse)
    
    renderWithRouter(<Login />)
    
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))
    
    await waitFor(() => {
      expect(api.authAPI.login).toHaveBeenCalledWith({
        email: 'test@example.com',
        password: 'password123'
      })
    })
  })

  it('should navigate to dashboard on successful login', async () => {
    const mockResponse = { data: { token: 'test-token' } }
    api.authAPI.login.mockResolvedValue(mockResponse)
    
    renderWithRouter(<Login />)
    
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))
    
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
    })
  })

  it('should show error on failed login', async () => {
    api.authAPI.login.mockRejectedValue({
      response: { data: { message: 'Invalid credentials' } }
    })
    
    renderWithRouter(<Login />)
    
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'wrongpassword' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))
    
    await waitFor(() => {
      expect(screen.getByText('Invalid credentials')).toBeInTheDocument()
    })
  })

  it('should show loading state while logging in', async () => {
    const mockResponse = { data: { token: 'test-token' } }
    api.authAPI.login.mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve(mockResponse), 100))
    )
    
    renderWithRouter(<Login />)
    
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Login' }))
    
    expect(screen.getByRole('button', { name: 'Logging in...' })).toBeInTheDocument()
  })

  it('should have link to register page', () => {
    renderWithRouter(<Login />)
    expect(screen.getByText('Register').closest('a')).toHaveAttribute('href', '/register')
  })
})
