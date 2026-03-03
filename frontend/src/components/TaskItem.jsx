function TaskItem({ task, onEdit, onDelete }) {
  const statusClass = `status-${task.status.toLowerCase().replace('_', '-')}`

  return (
    <div className={`task-item ${statusClass}`}>
      <div className="task-header">
        <h3 className="task-title">{task.title}</h3>
        <span className={`task-status ${statusClass}`}>
          {task.status.replace('_', ' ')}
        </span>
      </div>
      
      {task.description && (
        <p className="task-description">{task.description}</p>
      )}
      
      <div className="task-meta">
        <span className="task-date">
          Created: {new Date(task.createdAt).toLocaleDateString()}
        </span>
      </div>

      <div className="task-actions">
        <button 
          onClick={() => onEdit(task)} 
          className="btn btn-edit"
        >
          Edit
        </button>
        <button 
          onClick={() => onDelete(task.id)} 
          className="btn btn-delete"
        >
          Delete
        </button>
      </div>
    </div>
  )
}

export default TaskItem
