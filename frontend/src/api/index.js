import axios from 'axios'
import { ElMessage } from 'element-plus'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true, // 自动携带 httpOnly Cookie（Token 由后端通过 Cookie 下发）
})

// 是否正在刷新 Token
let isRefreshing = false
let refreshSubscribers = []

function subscribeTokenRefresh(cb) {
  refreshSubscribers.push(cb)
}

function onTokenRefreshed() {
  refreshSubscribers.forEach(cb => cb())
  refreshSubscribers = []
}

// 响应拦截：统一处理业务错误 + Token 静默刷新
api.interceptors.response.use(
  (res) => {
    if (res.data.code !== 0) {
      ElMessage.error(res.data.message || '操作失败')
      return Promise.reject(res.data)
    }
    return res.data
  },
  async (err) => {
    const originalRequest = err.config

    // 如果是 4005（权限不足/Token 失效）且未重试过，尝试刷新 Token
    if (err.response?.data?.code === 4005 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh(() => resolve(api(originalRequest)))
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        await axios.post('/api/refresh', {}, { withCredentials: true })
        isRefreshing = false
        onTokenRefreshed()
        return api(originalRequest)
      } catch (refreshErr) {
        isRefreshing = false
        refreshSubscribers = []
        localStorage.removeItem('user')
        localStorage.removeItem('uid')
        window.location.href = '/login'
        return Promise.reject(refreshErr)
      }
    }

    ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    return Promise.reject(err.response?.data || err)
  }
)

// ============ 登录 ============
export const login = (data) => api.post('/login', data)
export const logout = () => api.post('/logout')
export const refreshToken = () => api.post('/refresh')

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
