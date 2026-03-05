import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import TaskItem from './TaskItem'

describe('TaskItem', () => {
  const mockTask = {
    id: 1,
    title: 'Test Task',
    description: 'Test Description',
    status: 'TODO',
    createdAt: '2024-01-01T00:00:00.000Z',
    userId: 1,
    userName: 'Test User'
  }

  const mockOnEdit = vi.fn()
  const mockOnDelete = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render task title', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('Test Task')).toBeInTheDocument()
  })

  it('should render task description', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('Test Description')).toBeInTheDocument()
  })

  it('should render task status', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('TODO')).toBeInTheDocument()
  })

  it('should render edit and delete buttons', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('Edit')).toBeInTheDocument()
    expect(screen.getByText('Delete')).toBeInTheDocument()
  })

  it('should call onEdit when edit button is clicked', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    fireEvent.click(screen.getByText('Edit'))
    expect(mockOnEdit).toHaveBeenCalledWith(mockTask)
  })

  it('should call onDelete when delete button is clicked', () => {
    render(<TaskItem task={mockTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    fireEvent.click(screen.getByText('Delete'))
    expect(mockOnDelete).toHaveBeenCalledWith(mockTask.id)
  })

  it('should not render description when not provided', () => {
    const taskWithoutDescription = { ...mockTask, description: null }
    render(<TaskItem task={taskWithoutDescription} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.queryByText('Test Description')).not.toBeInTheDocument()
  })

  it('should render IN_PROGRESS status correctly', () => {
    const inProgressTask = { ...mockTask, status: 'IN_PROGRESS' }
    render(<TaskItem task={inProgressTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('IN PROGRESS')).toBeInTheDocument()
  })

  it('should render DONE status correctly', () => {
    const doneTask = { ...mockTask, status: 'DONE' }
    render(<TaskItem task={doneTask} onEdit={mockOnEdit} onDelete={mockOnDelete} />)
    expect(screen.getByText('DONE')).toBeInTheDocument()
  })
})
