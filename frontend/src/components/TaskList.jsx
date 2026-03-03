import TaskItem from './TaskItem'

function TaskList({ tasks, onEdit, onDelete, loading }) {
  if (loading) {
    return <div className="loading">Loading tasks...</div>
  }

  if (tasks.length === 0) {
    return (
      <div className="empty-state">
        <p>No tasks found. Create your first task!</p>
      </div>
    )
  }

  return (
    <div className="task-list">
      {tasks.map((task) => (
        <TaskItem
          key={task.id}
          task={task}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      ))}
    </div>
  )
}

export default TaskList
