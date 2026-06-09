export function useStatus() {
  const statusConfig = {
    PENDING: {
      label: '待执行',
      color: '#f59e0b',
      bg: 'rgba(245, 158, 11, 0.1)',
      icon: '⏳',
    },
    IN_PROGRESS: {
      label: '执行中',
      color: '#3b82f6',
      bg: 'rgba(59, 130, 246, 0.1)',
      icon: '⚡',
    },
    PAUSED: {
      label: '暂停',
      color: '#f97316',
      bg: 'rgba(249, 115, 22, 0.1)',
      icon: '⏸',
    },
    ERROR_PAUSED: {
      label: '错误暂停',
      color: '#ef4444',
      bg: 'rgba(239, 68, 68, 0.1)',
      icon: '⚠️',
    },
    COMPLETED: {
      label: '已完成',
      color: '#10b981',
      bg: 'rgba(16, 185, 129, 0.1)',
      icon: '✅',
    },
  }

  const getStatusLabel = (status) => statusConfig[status]?.label || status
  const getStatusColor = (status) => statusConfig[status]?.color || '#6b7280'
  const getStatusBg = (status) => statusConfig[status]?.bg || 'rgba(107, 114, 128, 0.1)'
  const getStatusIcon = (status) => statusConfig[status]?.icon || '❓'

  const formatTime = (t) => {
    if (!t) return '-'
    return new Date(t).toLocaleString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
    })
  }

  return {
    statusConfig,
    getStatusLabel,
    getStatusColor,
    getStatusBg,
    getStatusIcon,
    formatTime,
  }
}
