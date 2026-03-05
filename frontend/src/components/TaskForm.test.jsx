import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent, waitFor } from '@testing-library/react'
import TaskForm from './TaskForm'

describe('TaskForm', () => {
  const mockOnSubmit = vi.fn().mockResolvedValue(undefined)

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render form fields', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    expect(screen.getByLabelText('Title')).toBeInTheDocument()
    expect(screen.getByLabelText('Description')).toBeInTheDocument()
    expect(screen.getByLabelText('Status')).toBeInTheDocument()
  })

  it('should render Create Task button', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    expect(screen.getByRole('button', { name: 'Create Task' })).toBeInTheDocument()
  })

  it('should render Update Task button when initialData is provided', () => {
    const initialData = { id: 1, title: 'Existing Task', description: 'Desc', status: 'TODO' }
    render(<TaskForm onSubmit={mockOnSubmit} initialData={initialData} />)
    expect(screen.getByRole('button', { name: 'Update Task' })).toBeInTheDocument()
  })

  it('should show error when title is empty', async () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    
    const titleInput = screen.getByLabelText('Title')
    fireEvent.change(titleInput, { target: { value: '' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Create Task' }))
    
    await waitFor(() => {
      expect(screen.getByText('Title is required')).toBeInTheDocument()
    })
    expect(mockOnSubmit).not.toHaveBeenCalled()
  })

  it('should call onSubmit with form data when valid', async () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    
    const titleInput = screen.getByLabelText('Title')
    fireEvent.change(titleInput, { target: { value: 'New Task' } })
    
    const descriptionInput = screen.getByLabelText('Description')
    fireEvent.change(descriptionInput, { target: { value: 'New Description' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Create Task' }))
    
    await waitFor(() => {
      expect(mockOnSubmit).toHaveBeenCalledWith({
        title: 'New Task',
        description: 'New Description',
        status: 'TODO'
      })
    })
  })

  it('should reset form after submission when creating new task', async () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    
    const titleInput = screen.getByLabelText('Title')
    fireEvent.change(titleInput, { target: { value: 'New Task' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Create Task' }))
    
    await waitFor(() => {
      expect(titleInput.value).toBe('')
    })
  })

  it('should not reset form when updating existing task', async () => {
    const initialData = { id: 1, title: 'Existing Task', description: 'Desc', status: 'TODO' }
    render(<TaskForm onSubmit={mockOnSubmit} initialData={initialData} />)
    
    const titleInput = screen.getByLabelText('Title')
    expect(titleInput.value).toBe('Existing Task')
    
    fireEvent.click(screen.getByRole('button', { name: 'Update Task' }))
    
    await waitFor(() => {
      expect(titleInput.value).toBe('Existing Task')
    })
  })

  it('should show loading state while submitting', async () => {
    const slowOnSubmit = vi.fn().mockImplementation(() => 
      new Promise(resolve => setTimeout(resolve, 100))
    )
    
    render(<TaskForm onSubmit={slowOnSubmit} />)
    
    const titleInput = screen.getByLabelText('Title')
    fireEvent.change(titleInput, { target: { value: 'New Task' } })
    
    fireEvent.click(screen.getByRole('button', { name: 'Create Task' }))
    
    expect(screen.getByRole('button', { name: 'Saving...' })).toBeInTheDocument()
    expect(screen.getByLabelText('Title')).toBeDisabled()
  })

  it('should show cancel button when onCancel is provided', () => {
    const mockOnCancel = vi.fn()
    render(<TaskForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />)
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument()
  })

  it('should call onCancel when cancel button is clicked', () => {
    const mockOnCancel = vi.fn()
    render(<TaskForm onSubmit={mockOnSubmit} onCancel={mockOnCancel} />)
    
    fireEvent.click(screen.getByRole('button', { name: 'Cancel' }))
    expect(mockOnCancel).toHaveBeenCalled()
  })

  it('should handle status change', () => {
    render(<TaskForm onSubmit={mockOnSubmit} />)
    
    const statusSelect = screen.getByLabelText('Status')
    fireEvent.change(statusSelect, { target: { value: 'IN_PROGRESS' } })
    
    expect(statusSelect.value).toBe('IN_PROGRESS')
  })
})
