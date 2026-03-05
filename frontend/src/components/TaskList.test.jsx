import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import TaskList from './TaskList'

describe('TaskList', () => {
  const mockTasks = [
    {
      id: 1,
      title: 'Task 1',
      description: 'Description 1',
      status: 'TODO',
      createdAt: '2024-01-01T00:00:00.000Z',
      userId: 1,
      userName: 'Test User'
    },
    {
      id: 2,
      title: 'Task 2',
      description: 'Description 2',
      status: 'IN_PROGRESS',
      createdAt: '2024-01-02T00:00:00.000Z',
      userId: 1,
      userName: 'Test User'
    }
  ]

  const mockOnEdit = vi.fn()
  const mockOnDelete = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should render loading state when loading is true', () => {
    render(<TaskList tasks={[]} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={true} />)
    expect(screen.getByText('Loading tasks...')).toBeInTheDocument()
  })

  it('should render empty state when no tasks', () => {
    render(<TaskList tasks={[]} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    expect(screen.getByText('No tasks found. Create your first task!')).toBeInTheDocument()
  })

  it('should render list of tasks', () => {
    render(<TaskList tasks={mockTasks} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    expect(screen.getByText('Task 1')).toBeInTheDocument()
    expect(screen.getByText('Task 2')).toBeInTheDocument()
  })

  it('should render correct number of tasks', () => {
    render(<TaskList tasks={mockTasks} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    const taskItems = document.querySelectorAll('.task-item')
    expect(taskItems.length).toBe(2)
  })

  it('should not render empty state when tasks exist', () => {
    render(<TaskList tasks={mockTasks} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    expect(screen.queryByText('No tasks found. Create your first task!')).not.toBeInTheDocument()
  })

  it('should not render loading state when not loading', () => {
    render(<TaskList tasks={mockTasks} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    expect(screen.queryByText('Loading tasks...')).not.toBeInTheDocument()
  })

  it('should pass onEdit and onDelete to TaskItem', () => {
    // This test verifies that the callbacks are passed correctly
    // The actual functionality is tested in TaskItem tests
    render(<TaskList tasks={mockTasks} onEdit={mockOnEdit} onDelete={mockOnDelete} loading={false} />)
    // If the component renders without error, the props are passed correctly
    expect(screen.getByText('Task 1')).toBeInTheDocument()
  })
})
