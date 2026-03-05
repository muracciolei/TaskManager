import { describe, it, expect, vi, beforeEach } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import { BrowserRouter } from 'react-router-dom'
import Register from './Register'
import { AuthProvider } from '../context/AuthContext'
import * as api from '../services/api'

// Mock the API
vi.mock('../services/api', () => ({
  authAPI: {
    register: vi.fn(),
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

describe('Register', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
    mockNavigate.mockClear()
  })

  it('should render registration form', () => {
    renderWithRouter(<Register />)
    expect(screen.getByLabelText('Name')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Register' })).toBeInTheDocument()
  })

  it('should show error when fields are empty', async () => {
    renderWithRouter(<Register />)
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    await waitFor(() => {
      expect(screen.getByText('Please fill in all fields')).toBeInTheDocument()
    })
  })

  it('should show error when password is too short', async () => {
    renderWithRouter(<Register />)
    
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test User' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: '12345' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    await waitFor(() => {
      expect(screen.getByText('Password must be at least 6 characters')).toBeInTheDocument()
    })
  })

  it('should call register API with correct data', async () => {
    const mockRegisterResponse = { data: { token: 'test-token' } }
    const mockLoginResponse = { data: { token: 'test-token' } }
    
    api.authAPI.register.mockResolvedValue(mockRegisterResponse)
    api.authAPI.login.mockResolvedValue(mockLoginResponse)
    
    renderWithRouter(<Register />)
    
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test User' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    await waitFor(() => {
      expect(api.authAPI.register).toHaveBeenCalledWith({
        name: 'Test User',
        email: 'test@example.com',
        password: 'password123'
      })
    })
  })

  it('should navigate to dashboard on successful registration', async () => {
    const mockRegisterResponse = { data: { token: 'test-token' } }
    const mockLoginResponse = { data: { token: 'test-token' } }
    
    api.authAPI.register.mockResolvedValue(mockRegisterResponse)
    api.authAPI.login.mockResolvedValue(mockLoginResponse)
    
    renderWithRouter(<Register />)
    
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test User' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard')
    })
  })

  it('should show error on failed registration', async () => {
    api.authAPI.register.mockRejectedValue({
      response: { data: { message: 'Email already exists' } }
    })
    
    renderWithRouter(<Register />)
    
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test User' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'existing@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    await waitFor(() => {
      expect(screen.getByText('Email already exists')).toBeInTheDocument()
    })
  })

  it('should show loading state while registering', async () => {
    const mockRegisterResponse = { data: { token: 'test-token' } }
    const mockLoginResponse = { data: { token: 'test-token' } }
    
    api.authAPI.register.mockImplementation(() => 
      new Promise(resolve => setTimeout(() => resolve(mockRegisterResponse), 100))
    )
    api.authAPI.login.mockResolvedValue(mockLoginResponse)
    
    renderWithRouter(<Register />)
    
    fireEvent.change(screen.getByLabelText('Name'), { target: { value: 'Test User' } })
    fireEvent.change(screen.getByLabelText('Email'), { target: { value: 'test@example.com' } })
    fireEvent.change(screen.getByLabelText('Password'), { target: { value: 'password123' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Register' }))
    
    expect(screen.getByRole('button', { name: 'Registering...' })).toBeInTheDocument()
  })

  it('should have link to login page', () => {
    renderWithRouter(<Register />)
    expect(screen.getByText('Login').closest('a')).toHaveAttribute('href', '/login')
  })
})
