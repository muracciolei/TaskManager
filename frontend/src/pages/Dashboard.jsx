import { useState, useEffect, useCallback } from 'react'
import { tasksAPI } from '../services/api'
import TaskList from '../components/TaskList'
import TaskForm from '../components/TaskForm'

function Dashboard() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [editingTask, setEditingTask] = useState(null)

  const fetchTasks = useCallback(async () => {
    setLoading(true)
    try {
      const response = statusFilter 
        ? await tasksAPI.getByStatus(statusFilter)
        : await tasksAPI.getAll()
      setTasks(response.data)
      setError('')
    } catch (err) {
      setError('Failed to fetch tasks')
    } finally {
      setLoading(false)
    }
  }, [statusFilter])

  useEffect(() => {
    fetchTasks()
  }, [fetchTasks])

  const handleCreateTask = async (data) => {
    try {
      await tasksAPI.create(data)
      await fetchTasks()
      setShowForm(false)
    } catch (err) {
      throw new Error(err.response?.data?.message || 'Failed to create task')
    }
  }

  const handleUpdateTask = async (data) => {
    try {
      await tasksAPI.update(editingTask.id, data)
      await fetchTasks()
      setEditingTask(null)
    } catch (err) {
      throw new Error(err.response?.data?.message || 'Failed to update task')
    }
  }

  const handleDeleteTask = async (id) => {
    if (!window.confirm('Are you sure you want to delete this task?')) return
    
    try {
      await tasksAPI.delete(id)
      await fetchTasks()
    } catch (err) {
      setError('Failed to delete task')
    }
  }

  const handleEdit = (task) => {
    setEditingTask(task)
    setShowForm(false)
  }

  const handleCancelEdit = () => {
    setEditingTask(null)
  }

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>My Tasks</h1>
        <button 
          onClick={() => { setShowForm(!showForm); setEditingTask(null) }} 
          className="btn btn-primary"
        >
          {showForm ? 'Cancel' : 'New Task'}
        </button>
      </div>

      <div className="filters">
        <label htmlFor="status-filter">Filter by status:</label>
        <select
          id="status-filter"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
        >
          <option value="">All</option>
          <option value="TODO">To Do</option>
          <option value="IN_PROGRESS">In Progress</option>
          <option value="DONE">Done</option>
        </select>
      </div>

      {error && <div className="error-message">{error}</div>}

      <div className="dashboard-content">
        {(showForm || editingTask) && (
          <div className="task-form-container">
            <h2>{editingTask ? 'Edit Task' : 'Create New Task'}</h2>
            <TaskForm
              onSubmit={editingTask ? handleUpdateTask : handleCreateTask}
              initialData={editingTask}
              onCancel={editingTask ? handleCancelEdit : () => setShowForm(false)}
            />
          </div>
        )}

        <TaskList
          tasks={tasks}
          onEdit={handleEdit}
          onDelete={handleDeleteTask}
          loading={loading}
        />
      </div>
    </div>
  )
}

export default Dashboard
