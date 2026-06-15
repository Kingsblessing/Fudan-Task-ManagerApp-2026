<template>
  <div :class="['worker-page', { 'dark-mode': isDark }]">
    <!-- Header -->
    <header class="app-header glass">
      <div class="header-left">
        <div class="logo-sm">
          <svg width="32" height="32" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="var(--accent)" />
            <path d="M15 24l6 6 12-12" stroke="white" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div>
          <h1 class="header-title">Worker 工作台</h1>
          <p class="header-sub">{{ isDark ? '深色' : '浅色' }}模式 · {{ viewModeLabel }}视图</p>
        </div>
      </div>
      <div class="header-right">
        <div class="user-badge">
          <span class="badge-icon">⚡</span>
          <span class="badge-id">ID: {{ user?.userId }}</span>
        </div>
        <button class="logout-btn" @click="logout">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
          退出
        </button>
      </div>
    </header>

    <div class="main-layout">
      <!-- Sidebar -->
      <nav class="sidebar glass">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          :class="['nav-item', { active: taskType === tab.key }]"
          @click="switchType(tab.key)"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
          <span v-if="tab.count > 0" class="nav-badge">{{ tab.count }}</span>
        </button>
      </nav>

      <!-- Content -->
      <main class="content">
        <div class="tab-content">
          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <h2 class="section-title">{{ currentTabLabel }}</h2>
              <span class="count-badge">{{ tasks.length }} 个任务</span>
            </div>
          </div>

          <!-- Table View -->
          <div v-if="isTableView" class="table-view animate-stagger">
            <div class="table-wrapper glass">
              <table class="data-table" v-if="tasks.length > 0">
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>标题</th>
                    <th>描述</th>
                    <th>状态</th>
                    <th>执行人</th>
                    <th>操作</th>
                  </tr>
                </thead>
                <tbody>
                  <tr
                    v-for="task in tasks"
                    :key="task.id"
                    class="table-row"
                    @click="goDetail(task.id)"
                  >
                    <td class="cell-id">
                      <span class="id-badge">#{{ task.id }}</span>
                    </td>
                    <td class="cell-title">{{ task.title }}</td>
                    <td class="cell-desc">{{ task.description || '-' }}</td>
                    <td>
                      <span
                        class="status-pill"
                        :style="{ color: getStatusColor(task.status), background: getStatusBg(task.status) }"
                      >
                        {{ getStatusIcon(task.status) }} {{ getStatusLabel(task.status) }}
                      </span>
                    </td>
                    <td class="cell-assignee">{{ task.assigneeId ?? '-' }}</td>
                    <td @click.stop>
                      <div class="action-group">
                        <button
                          v-if="taskType === 'claimable'"
                          class="action-btn claim"
                          @click="handleClaim(task.id)"
                        >
                          认领
                        </button>
                        <template v-if="taskType === 'processing'">
                          <button class="action-btn finish" @click="handleFinish(task.id)">完成</button>
                          <button class="action-btn error" @click="showErrorDialog(task.id)">错误暂停</button>
                        </template>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="empty-state">
                <span class="empty-icon">📭</span>
                <p>暂无任务</p>
              </div>
            </div>
          </div>

          <!-- Card View -->
          <div v-if="isCardView" class="card-view">
            <div v-if="tasks.length > 0" class="task-grid animate-stagger">
              <div
                v-for="task in tasks"
                :key="task.id"
                class="task-card glass glow-border"
                @click="goDetail(task.id)"
              >
                <div class="card-header-row">
                  <span class="card-id">#{{ task.id }}</span>
                  <span
                    class="status-badge"
                    :style="{ color: getStatusColor(task.status), borderColor: getStatusColor(task.status) }"
                  >
                    {{ getStatusIcon(task.status) }} {{ getStatusLabel(task.status) }}
                  </span>
                </div>
                <h3 class="card-title">{{ task.title }}</h3>
                <p class="card-desc">{{ task.description || '暂无描述' }}</p>
                <div class="card-meta">
                  <span class="meta-item">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                      <circle cx="12" cy="7" r="4"/>
                    </svg>
                    {{ task.assigneeId ?? '未认领' }}
                  </span>
                </div>
                <div class="card-actions" @click.stop>
                  <button
                    v-if="taskType === 'claimable'"
                    class="card-action-btn claim"
                    @click="handleClaim(task.id)"
                  >
                    认领任务
                  </button>
                  <template v-if="taskType === 'processing'">
                    <button class="card-action-btn finish" @click="handleFinish(task.id)">完成</button>
                    <button class="card-action-btn error" @click="showErrorDialog(task.id)">错误暂停</button>
                  </template>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">📭</span>
              <p>暂无任务</p>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Error Pause Dialog -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="errorDialogVisible" class="modal-overlay" @click.self="errorDialogVisible = false">
          <div class="modal-card glass">
            <div class="modal-header">
              <h2>错误暂停</h2>
              <button class="modal-close" @click="errorDialogVisible = false">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="handleErrorPause" class="modal-form">
              <div class="form-group">
                <label class="form-label">错误原因</label>
                <textarea
                  v-model="errorMessage"
                  placeholder="请详细描述遇到的错误..."
                  class="form-textarea"
                  rows="4"
                  required
                ></textarea>
              </div>
              <div class="modal-actions">
                <button type="button" class="btn-cancel" @click="errorDialogVisible = false">取消</button>
                <button type="submit" class="btn-submit error" :disabled="submitting">
                  <span v-if="submitting" class="spinner"></span>
                  <span v-else>提交错误报告</span>
                </button>
              </div>
            </form>
          </div>
        </div>
      </transition>
    </teleport>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useToast } from '../composables/useToast'
import { useStatus } from '../composables/useStatus'
import { getWorkerTasks, claimTask, finishTask, errorPauseTask } from '../api'

const router = useRouter()
const { isDark, isTableView, isCardView, viewModeLabel } = useTheme()
const toast = useToast()
const { getStatusLabel, getStatusColor, getStatusBg, getStatusIcon } = useStatus()

const user = JSON.parse(localStorage.getItem('user'))
const taskType = ref('claimable')
const tasks = ref([])
const loading = ref(false)
const submitting = ref(false)
const errorDialogVisible = ref(false)
const errorMessage = ref('')
const errorTargetId = ref(null)

// 为每个 tab 维护独立的任务数量
const taskCounts = ref({
  claimable: 0,
  processing: 0,
  assigned: 0,
})

const tabs = computed(() => [
  { key: 'claimable', label: '可认领任务', icon: '🎯', count: taskCounts.value.claimable },
  { key: 'processing', label: '进行中任务', icon: '⚡', count: taskCounts.value.processing },
  { key: 'assigned', label: '我的任务', icon: '📋', count: taskCounts.value.assigned },
])

const currentTabLabel = computed(() => tabs.value.find(t => t.key === taskType.value)?.label || '')

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getWorkerTasks(taskType.value)
    tasks.value = res.data || []
  } catch (e) {
    toast.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

// 获取所有 tab 的任务数量
const fetchAllCounts = async () => {
  try {
    const [claimableRes, processingRes, assignedRes] = await Promise.all([
      getWorkerTasks('claimable'),
      getWorkerTasks('processing'),
      getWorkerTasks('assigned'),
    ])
    taskCounts.value = {
      claimable: claimableRes.data?.length || 0,
      processing: processingRes.data?.length || 0,
      assigned: assignedRes.data?.length || 0,
    }
  } catch (e) {
    console.error('获取任务数量失败', e)
  }
}

const switchType = (type) => {
  taskType.value = type
  fetchTasks()
  fetchAllCounts() // 切换 tab 时更新所有数量
}

const handleClaim = async (id) => {
  if (!confirm('确认认领该任务？')) return
  try {
    await claimTask(id)
    toast.success('认领成功')
    fetchTasks()
    fetchAllCounts() // 更新所有数量
  } catch (e) {
    toast.error(e.message || '认领失败')
  }
}

const handleFinish = async (id) => {
  if (!confirm('确认完成该任务？')) return
  try {
    await finishTask(id)
    toast.success('任务已完成')
    fetchTasks()
    fetchAllCounts() // 更新所有数量
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}

const showErrorDialog = (id) => {
  errorTargetId.value = id
  errorMessage.value = ''
  errorDialogVisible.value = true
}

const handleErrorPause = async () => {
  if (!errorMessage.value.trim()) {
    toast.warning('请输入错误原因')
    return
  }
  submitting.value = true
  try {
    await errorPauseTask(errorTargetId.value, errorMessage.value)
    toast.success('已上报错误')
    errorDialogVisible.value = false
    fetchTasks()
    fetchAllCounts() // 更新所有数量
  } catch (e) {
    toast.error(e.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const goDetail = (id) => {
  router.push(`/task/${id}`)
}

const logout = async () => {
  try {
    const { logout: apiLogout } = await import('../api')
    await apiLogout()
  } catch (e) { /* 即使失败也清除本地状态 */ }
  localStorage.removeItem('user')
  localStorage.removeItem('uid')
  router.push('/login')
}

onMounted(() => {
  fetchTasks()
  fetchAllCounts()
})
</script>

<style scoped>
.worker-page {
  min-height: 100vh;
  background: var(--bg-primary);
}

/* ===== Header ===== */
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 28px;
  border-bottom: 1px solid var(--border);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo-sm svg {
  display: block;
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.header-sub {
  font-size: 11px;
  color: var(--text-tertiary);
  text-transform: uppercase;
  letter-spacing: 1px;
  margin: 2px 0 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--bg-tertiary);
  border-radius: 999px;
  font-size: 13px;
}

.badge-icon {
  font-size: 16px;
}

.badge-id {
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-secondary);
}

.logout-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: transparent;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
}

.logout-btn:hover {
  color: #ef4444;
  border-color: #ef4444;
  background: rgba(239, 68, 68, 0.05);
}

/* ===== Layout ===== */
.main-layout {
  display: flex;
  min-height: calc(100vh - 65px);
}

/* ===== Sidebar ===== */
.sidebar {
  width: 200px;
  padding: 16px 12px;
  border-right: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex-shrink: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
  background: transparent;
  font-family: inherit;
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  background: var(--bg-tertiary);
  color: var(--text-primary);
}

.nav-item.active {
  background: var(--accent-light);
  color: var(--accent);
  font-weight: 600;
}

.nav-icon {
  font-size: 18px;
}

.nav-badge {
  margin-left: auto;
  padding: 2px 8px;
  background: var(--accent);
  color: white;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

/* ===== Content ===== */
.content {
  flex: 1;
  padding: 24px 28px;
  overflow-y: auto;
}

.tab-content {
  animation: fadeIn 0.3s ease-out;
}

/* ===== Toolbar ===== */
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.section-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.count-badge {
  padding: 4px 12px;
  background: var(--accent-light);
  color: var(--accent);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

/* ===== Crystal Table View ===== */
.table-wrapper {
  border-radius: var(--radius-lg);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th {
  padding: 14px 16px;
  text-align: left;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-tertiary);
  background: var(--bg-tertiary);
  border-bottom: 1px solid var(--border);
}

.data-table td {
  padding: 14px 16px;
  font-size: 14px;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border);
}

.table-row {
  cursor: pointer;
  transition: background 0.15s ease;
}

.table-row:hover {
  background: var(--accent-light);
}

.table-row:last-child td {
  border-bottom: none;
}

.cell-id {
  width: 80px;
}

.id-badge {
  display: inline-block;
  padding: 2px 10px;
  background: var(--accent-light);
  color: var(--accent);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
}

.cell-title {
  font-weight: 600;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.cell-desc {
  max-width: 250px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--text-secondary);
}

.cell-assignee {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.action-group {
  display: flex;
  gap: 6px;
}

.action-btn {
  padding: 6px 14px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.action-btn.claim {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.action-btn.claim:hover {
  background: #3b82f6;
  color: white;
}

.action-btn.finish {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.action-btn.finish:hover {
  background: #10b981;
  color: white;
}

.action-btn.error {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.action-btn.error:hover {
  background: #ef4444;
  color: white;
}

/* ===== Obsidian Card View ===== */
.task-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
}

.task-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 1px solid var(--border);
}

.task-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--accent);
}

.card-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-id {
  font-size: 12px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-tertiary);
}

.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 3px 10px;
  border: 1px solid;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}

.card-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0 0 14px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.card-actions {
  display: flex;
  gap: 8px;
}

.card-action-btn {
  flex: 1;
  padding: 8px 14px;
  border: 1px solid;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  background: transparent;
}

.card-action-btn.claim {
  color: #3b82f6;
  border-color: #3b82f6;
}

.card-action-btn.claim:hover {
  background: #3b82f6;
  color: white;
}

.card-action-btn.finish {
  color: #10b981;
  border-color: #10b981;
}

.card-action-btn.finish:hover {
  background: #10b981;
  color: white;
}

.card-action-btn.error {
  color: #ef4444;
  border-color: #ef4444;
}

.card-action-btn.error:hover {
  background: #ef4444;
  color: white;
}

/* ===== Empty State ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  color: var(--text-tertiary);
  margin: 0;
}

/* ===== Modal ===== */
.modal-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  padding: 20px;
}

.modal-card {
  width: 100%;
  max-width: 480px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  animation: scaleIn 0.3s ease-out;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 28px 0;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.modal-close {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
}

.modal-close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #ef4444;
}

.modal-form {
  padding: 24px 28px 28px;
}

.form-group {
  margin-bottom: 18px;
}

.form-label {
  display: block;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}

.form-textarea {
  width: 100%;
  padding: 12px 14px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  transition: all 0.2s ease;
  resize: vertical;
}

.form-textarea:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px var(--accent-light);
}

.form-textarea::placeholder {
  color: var(--text-tertiary);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.btn-cancel {
  padding: 10px 20px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-cancel:hover {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-submit {
  padding: 10px 24px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 8px;
}

.btn-submit.error {
  background: #ef4444;
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Modal Transitions ===== */
.modal-enter-active {
  transition: all 0.3s ease-out;
}

.modal-leave-active {
  transition: all 0.2s ease-in;
}

.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-card {
  transform: scale(0.95) translateY(10px);
}

/* ===== Animations ===== */
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

@keyframes scaleIn {
  from { transform: scale(0.95); opacity: 0; }
  to { transform: scale(1); opacity: 1; }
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }

  .app-header {
    padding: 12px 16px;
    flex-wrap: wrap;
    gap: 8px;
  }

  .header-left {
    gap: 10px;
  }

  .header-title {
    font-size: 15px;
  }

  .content {
    padding: 16px;
  }

  .toolbar {
    flex-direction: column;
    align-items: flex-start;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }

  .data-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 10px 8px;
  }
}
</style>
