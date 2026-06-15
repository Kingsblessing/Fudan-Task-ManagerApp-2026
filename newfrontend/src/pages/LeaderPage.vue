<template>
  <div :class="['leader-page', { 'dark-mode': isDark }]">
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
          <h1 class="header-title">Leader 管理面板</h1>
          <p class="header-sub">{{ isDark ? '深色' : '浅色' }}模式 · {{ viewModeLabel }}视图</p>
        </div>
      </div>
      <div class="header-right">
        <div class="user-badge">
          <span class="badge-icon">👑</span>
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
          :class="['nav-item', { active: activeTab === tab.key }]"
          @click="activeTab = tab.key"
        >
          <span class="nav-icon">{{ tab.icon }}</span>
          <span class="nav-label">{{ tab.label }}</span>
          <span v-if="tab.key === 'tasks' && tasks.length" class="nav-badge">{{ tasks.length }}</span>
        </button>
      </nav>

      <!-- Content -->
      <main class="content">
        <!-- ============ Tasks Tab ============ -->
        <div v-if="activeTab === 'tasks'" class="tab-content">
          <!-- Toolbar -->
          <div class="toolbar">
            <div class="toolbar-left">
              <h2 class="section-title">任务列表</h2>
              <span class="count-badge">{{ tasks.length }} 个任务</span>
            </div>
            <div class="toolbar-right">
              <div class="filter-group">
                <select v-model="filters.status" class="filter-select" @change="fetchTasks">
                  <option value="">全部状态</option>
                  <option value="PENDING">待执行</option>
                  <option value="IN_PROGRESS">执行中</option>
                  <option value="PAUSED">暂停</option>
                  <option value="ERROR_PAUSED">错误暂停</option>
                  <option value="COMPLETED">已完成</option>
                </select>
                <div class="search-box">
                  <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <circle cx="11" cy="11" r="8"/>
                    <line x1="21" y1="21" x2="16.65" y2="16.65"/>
                  </svg>
                  <input
                    v-model="filters.keyword"
                    placeholder="搜索任务..."
                    class="search-input"
                    @keyup.enter="fetchTasks"
                  />
                </div>
              </div>
              <button class="create-btn" @click="showCreateDialog = true">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"/>
                  <line x1="5" y1="12" x2="19" y2="12"/>
                </svg>
                新建任务
              </button>
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
                          v-if="task.status === 'PENDING' || task.status === 'IN_PROGRESS'"
                          class="action-btn pause"
                          @click="handlePause(task.id)"
                        >
                          暂停
                        </button>
                        <button
                          v-if="task.status === 'PAUSED' || task.status === 'ERROR_PAUSED'"
                          class="action-btn resume"
                          @click="handleResume(task.id)"
                        >
                          恢复
                        </button>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="empty-state">
                <span class="empty-icon">📋</span>
                <p>暂无任务</p>
                <button class="create-btn-sm" @click="showCreateDialog = true">创建第一个任务</button>
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
                    v-if="task.status === 'PENDING' || task.status === 'IN_PROGRESS'"
                    class="card-action-btn pause"
                    @click="handlePause(task.id)"
                  >
                    暂停
                  </button>
                  <button
                    v-if="task.status === 'PAUSED' || task.status === 'ERROR_PAUSED'"
                    class="card-action-btn resume"
                    @click="handleResume(task.id)"
                  >
                    恢复
                  </button>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">📋</span>
              <p>暂无任务</p>
              <button class="create-btn-sm" @click="showCreateDialog = true">创建第一个任务</button>
            </div>
          </div>
        </div>

        <!-- ============ Workers Tab ============ -->
        <div v-if="activeTab === 'workers'" class="tab-content">
          <div class="toolbar">
            <div class="toolbar-left">
              <h2 class="section-title">Worker 列表</h2>
              <span class="count-badge">{{ workers.length }} 位 Worker</span>
            </div>
          </div>

          <!-- Table View -->
          <div v-if="isTableView" class="table-view animate-stagger">
            <div class="table-wrapper glass">
              <table class="data-table" v-if="workers.length > 0">
                <thead>
                  <tr>
                    <th>Worker ID</th>
                    <th>姓名</th>
                    <th>被指派</th>
                    <th>进行中</th>
                    <th>已完成</th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="w in workers" :key="w.workerId" class="table-row">
                    <td class="cell-id">
                      <span class="id-badge worker">{{ w.workerId }}</span>
                    </td>
                    <td class="cell-name">{{ w.workerName }}</td>
                    <td>
                      <span class="stat-num">{{ w.assignedCount }}</span>
                    </td>
                    <td>
                      <span class="stat-num progress">{{ w.processingCount }}</span>
                    </td>
                    <td>
                      <span class="stat-num done">{{ w.completedCount }}</span>
                    </td>
                  </tr>
                </tbody>
              </table>
              <div v-else class="empty-state">
                <span class="empty-icon">👥</span>
                <p>暂无 Worker</p>
              </div>
            </div>
          </div>

          <!-- Card View -->
          <div v-if="isCardView" class="card-view">
            <div v-if="workers.length > 0" class="worker-grid animate-stagger">
              <div v-for="w in workers" :key="w.workerId" class="worker-card glass glow-border">
                <div class="worker-avatar">
                  <span>⚡</span>
                </div>
                <div class="worker-info">
                  <h3 class="worker-name">{{ w.workerName }}</h3>
                  <p class="worker-id">ID: {{ w.workerId }}</p>
                </div>
                <div class="worker-stats">
                  <div class="stat-item">
                    <span class="stat-val">{{ w.assignedCount }}</span>
                    <span class="stat-label">被指派</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-val progress">{{ w.processingCount }}</span>
                    <span class="stat-label">进行中</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-val done">{{ w.completedCount }}</span>
                    <span class="stat-label">已完成</span>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="empty-state">
              <span class="empty-icon">👥</span>
              <p>暂无 Worker</p>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- Create Task Dialog -->
    <teleport to="body">
      <transition name="modal">
        <div v-if="showCreateDialog" class="modal-overlay" @click.self="showCreateDialog = false">
          <div class="modal-card glass">
            <div class="modal-header">
              <h2>新建任务</h2>
              <button class="modal-close" @click="showCreateDialog = false">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="18" y1="6" x2="6" y2="18"/>
                  <line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
              </button>
            </div>
            <form @submit.prevent="handleCreate" class="modal-form">
              <div class="form-group">
                <label class="form-label">任务标题</label>
                <input
                  v-model="createForm.title"
                  placeholder="输入任务标题"
                  class="form-input"
                  required
                />
              </div>
              <div class="form-group">
                <label class="form-label">任务描述</label>
                <textarea
                  v-model="createForm.description"
                  placeholder="输入任务描述"
                  class="form-textarea"
                  rows="3"
                  required
                ></textarea>
              </div>
              <div class="form-group">
                <label class="form-label">候选 Worker</label>
                <div class="worker-select">
                  <label
                    v-for="w in workerOptions"
                    :key="w.workerId"
                    :class="['select-chip', { selected: createForm.candidateWorkerIds.includes(w.workerId) }]"
                  >
                    <input
                      type="checkbox"
                      :value="w.workerId"
                      v-model="createForm.candidateWorkerIds"
                      class="sr-only"
                    />
                    <span class="chip-name">{{ w.workerName }}</span>
                    <span class="chip-id">{{ w.workerId }}</span>
                  </label>
                </div>
              </div>
              <div class="modal-actions">
                <button type="button" class="btn-cancel" @click="showCreateDialog = false">取消</button>
                <button type="submit" class="btn-submit" :disabled="creating">
                  <span v-if="creating" class="spinner"></span>
                  <span v-else>创建任务</span>
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
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useToast } from '../composables/useToast'
import { useStatus } from '../composables/useStatus'
import { getLeaderTasks, getWorkers, createTask, pauseTask, resumeTask } from '../api'

const router = useRouter()
const { isDark, isTableView, isCardView, viewModeLabel } = useTheme()
const toast = useToast()
const { getStatusLabel, getStatusColor, getStatusBg, getStatusIcon } = useStatus()

const user = JSON.parse(localStorage.getItem('user'))
const activeTab = ref('tasks')
const loading = ref(false)
const creating = ref(false)
const tasks = ref([])
const workers = ref([])
const workerOptions = ref([])
const showCreateDialog = ref(false)

const tabs = [
  { key: 'tasks', label: '任务列表', icon: '📋' },
  { key: 'workers', label: 'Worker 列表', icon: '👥' },
]

const filters = reactive({ status: '', keyword: '' })
const createForm = reactive({ title: '', description: '', candidateWorkerIds: [] })

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getLeaderTasks(filters)
    tasks.value = res.data || []
  } catch (e) {
    toast.error('获取任务列表失败')
  } finally {
    loading.value = false
  }
}

const fetchWorkers = async () => {
  loading.value = true
  try {
    const res = await getWorkers()
    workers.value = res.data || []
    workerOptions.value = res.data || []
  } catch (e) {
    toast.error('获取 Worker 列表失败')
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createForm.title || !createForm.description || createForm.candidateWorkerIds.length === 0) {
    toast.warning('请填写所有必填项')
    return
  }
  creating.value = true
  try {
    await createTask(createForm)
    toast.success('任务创建成功')
    showCreateDialog.value = false
    createForm.title = ''
    createForm.description = ''
    createForm.candidateWorkerIds = []
    fetchTasks()
  } catch (e) {
    toast.error(e.message || '创建失败')
  } finally {
    creating.value = false
  }
}

const handlePause = async (id) => {
  if (!confirm('确认暂停该任务？')) return
  try {
    await pauseTask(id)
    toast.success('已暂停')
    fetchTasks()
  } catch (e) {
    toast.error(e.message || '操作失败')
  }
}

const handleResume = async (id) => {
  try {
    await resumeTask(id)
    toast.success('已恢复')
    fetchTasks()
  } catch (e) {
    toast.error(e.message || '操作失败')
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
  fetchWorkers()
})
</script>

<style scoped>
.leader-page {
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
  position: relative;
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

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-select {
  padding: 8px 12px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-family: inherit;
  color: var(--text-primary);
  cursor: pointer;
  outline: none;
}

.filter-select:focus {
  border-color: var(--accent);
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
}

.search-input {
  border: none;
  background: transparent;
  font-size: 13px;
  font-family: inherit;
  color: var(--text-primary);
  outline: none;
  width: 140px;
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.create-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 18px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.create-btn:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px var(--accent-glow);
}

.create-btn-sm {
  padding: 8px 16px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  margin-top: 12px;
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

.id-badge.worker {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
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

.action-btn.pause {
  background: rgba(245, 158, 11, 0.1);
  color: #f59e0b;
}

.action-btn.pause:hover {
  background: #f59e0b;
  color: white;
}

.action-btn.resume {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.action-btn.resume:hover {
  background: #10b981;
  color: white;
}

.stat-num {
  display: inline-block;
  padding: 2px 10px;
  background: var(--bg-tertiary);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
}

.stat-num.progress {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
}

.stat-num.done {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
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

.card-action-btn.pause {
  color: #f59e0b;
  border-color: #f59e0b;
}

.card-action-btn.pause:hover {
  background: #f59e0b;
  color: white;
}

.card-action-btn.resume {
  color: #10b981;
  border-color: #10b981;
}

.card-action-btn.resume:hover {
  background: #10b981;
  color: white;
}

/* Worker Grid (Obsidian) */
.worker-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 16px;
}

.worker-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.worker-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
  border-color: var(--accent);
}

.worker-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent), #5c7cfa);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.worker-name {
  font-size: 16px;
  font-weight: 700;
  margin: 0;
  color: var(--text-primary);
}

.worker-id {
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-tertiary);
  margin: 0;
}

.worker-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid var(--border);
  width: 100%;
  justify-content: center;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-val {
  font-size: 20px;
  font-weight: 800;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-primary);
}

.stat-val.progress {
  color: #3b82f6;
}

.stat-val.done {
  color: #10b981;
}

.stat-label {
  font-size: 11px;
  color: var(--text-tertiary);
  font-weight: 500;
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
  max-width: 520px;
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

.form-input,
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

.form-input:focus,
.form-textarea:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px var(--accent-light);
}

.form-input::placeholder,
.form-textarea::placeholder {
  color: var(--text-tertiary);
}

.worker-select {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.select-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.select-chip:hover {
  border-color: var(--border-hover);
}

.select-chip.selected {
  border-color: var(--accent);
  background: var(--accent-light);
}

.chip-name {
  font-weight: 600;
  color: var(--text-primary);
}

.chip-id {
  font-size: 11px;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-tertiary);
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  border: 0;
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

.btn-submit:hover:not(:disabled) {
  background: var(--accent-hover);
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

  .toolbar-right {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-group {
    width: 100%;
    flex-wrap: wrap;
  }

  .filter-select,
  .search-box {
    flex: 1;
    min-width: 120px;
  }

  .search-input {
    width: 100%;
  }

  .create-btn {
    width: 100%;
    justify-content: center;
  }

  .task-grid {
    grid-template-columns: 1fr;
  }

  .worker-grid {
    grid-template-columns: 1fr;
  }

  .data-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 10px 8px;
  }

  .mobile-nav {
    display: flex;
  }
}

/* Mobile bottom nav */
@media (max-width: 768px) {
  .leader-page {
    padding-bottom: 60px;
  }
}
</style>
