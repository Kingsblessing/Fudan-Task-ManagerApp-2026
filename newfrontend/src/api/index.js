import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// Request interceptor: auto-inject user identity headers
api.interceptors.request.use((config) => {
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (user) {
    config.headers['X-User-Id'] = user.userId
    config.headers['X-User-Role'] = user.role
  }
  return config
})

// Response interceptor: unified error handling
api.interceptors.response.use(
  (res) => {
    if (res.data.code !== 0) {
      return Promise.reject(res.data)
    }
    return res.data
  },
  (err) => {
    return Promise.reject(err)
  }
)

// ============ Auth ============
export const login = (data) => api.post('/login', data)

// ============ Leader ============
export const createTask = (data) => api.post('/leader/task', data)
export const getLeaderTasks = (params) => api.get('/leader/tasks', { params })
export const pauseTask = (id) => api.post(`/leader/tasks/${id}/pause`)
export const resumeTask = (id) => api.post(`/leader/tasks/${id}/resume`)
export const getWorkers = () => api.get('/leader/workers')

// ============ Worker ============
export const getWorkerTasks = (type) => api.get('/worker/tasks', { params: { type } })
export const claimTask = (id) => api.post(`/worker/tasks/${id}/claim`)
export const finishTask = (id) => api.post(`/worker/tasks/${id}/finish`)
export const errorPauseTask = (id, errorMessage) =>
  api.post(`/worker/tasks/${id}/error-pause`, { errorMessage })

// ============ Common ============
export const getTaskDetail = (id) => api.get(`/tasks/${id}`)
