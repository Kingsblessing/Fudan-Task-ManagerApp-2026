<template>
  <div :class="['detail-page', { 'dark-mode': isDark }]">
    <!-- Header -->
    <header class="app-header glass">
      <div class="header-left">
        <button class="back-btn" @click="goBack">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="19" y1="12" x2="5" y2="12"/>
            <polyline points="12 19 5 12 12 5"/>
          </svg>
          返回
        </button>
        <div class="header-divider"></div>
        <h1 class="header-title">任务详情</h1>
      </div>
      <div class="header-right">
        <div class="user-badge">
          <span class="badge-icon">{{ user?.role === 'LEADER' ? '👑' : '⚡' }}</span>
          <span class="badge-id">ID: {{ user?.userId }}</span>
          <span class="badge-role">{{ user?.role }}</span>
        </div>
      </div>
    </header>

    <!-- Content -->
    <main class="content">
      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <p>加载中...</p>
      </div>

      <div v-else-if="task" class="detail-container animate-fade-in">
        <!-- Table/Descriptions View -->
        <div v-if="isTableView" class="crystal-detail">
          <div class="detail-card glass">
            <!-- Header -->
            <div class="detail-header">
              <div class="detail-title-row">
                <span class="detail-id">#{{ task.id }}</span>
                <span
                  class="status-pill lg"
                  :style="{ color: getStatusColor(task.status), background: getStatusBg(task.status) }"
                >
                  {{ getStatusIcon(task.status) }} {{ getStatusLabel(task.status) }}
                </span>
              </div>
              <h2 class="detail-title">{{ task.title }}</h2>
            </div>

            <!-- Body -->
            <div class="detail-body">
              <div class="detail-section">
                <h3 class="section-label">描述</h3>
                <p class="section-content">{{ task.description || '暂无描述' }}</p>
              </div>

              <div class="detail-grid">
                <div class="detail-item">
                  <span class="item-label">创建人</span>
                  <span class="item-value">
                    <span class="user-chip">{{ task.creatorId }}</span>
                  </span>
                </div>
                <div class="detail-item">
                  <span class="item-label">执行人</span>
                  <span class="item-value">
                    <span v-if="task.assigneeId" class="user-chip worker">{{ task.assigneeId }}</span>
                    <span v-else class="unassigned">未认领</span>
                  </span>
                </div>
                <div class="detail-item">
                  <span class="item-label">创建时间</span>
                  <span class="item-value mono">{{ formatTime(task.createdAt) }}</span>
                </div>
                <div class="detail-item">
                  <span class="item-label">更新时间</span>
                  <span class="item-value mono">{{ formatTime(task.updatedAt) }}</span>
                </div>
              </div>

              <div class="detail-section">
                <h3 class="section-label">候选 Worker</h3>
                <div class="candidate-list">
                  <span
                    v-for="id in (task.candidateWorkerIds || [])"
                    :key="id"
                    class="candidate-chip"
                  >
                    {{ id }}
                  </span>
                  <span v-if="!task.candidateWorkerIds?.length" class="empty-text">-</span>
                </div>
              </div>

              <div v-if="task.errorMessage" class="detail-section error-section">
                <h3 class="section-label error">错误信息</h3>
                <p class="error-content">{{ task.errorMessage }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Card View -->
        <div v-if="isCardView" class="obsidian-detail">
          <!-- Hero Card -->
          <div class="hero-card glass">
            <div class="hero-bg"></div>
            <div class="hero-content">
              <span class="hero-id">#{{ task.id }}</span>
              <h1 class="hero-title">{{ task.title }}</h1>
              <span
                class="hero-status"
                :style="{ color: getStatusColor(task.status), borderColor: getStatusColor(task.status) }"
              >
                {{ getStatusIcon(task.status) }} {{ getStatusLabel(task.status) }}
              </span>
            </div>
          </div>

          <!-- Info Cards -->
          <div class="info-grid">
            <div class="info-card glass glow-border">
              <div class="info-icon">📝</div>
              <div class="info-body">
                <span class="info-label">描述</span>
                <p class="info-text">{{ task.description || '暂无描述' }}</p>
              </div>
            </div>

            <div class="info-card glass glow-border">
              <div class="info-icon">👤</div>
              <div class="info-body">
                <span class="info-label">创建人</span>
                <p class="info-val">{{ task.creatorId }}</p>
              </div>
            </div>

            <div class="info-card glass glow-border">
              <div class="info-icon">⚡</div>
              <div class="info-body">
                <span class="info-label">执行人</span>
                <p class="info-val">{{ task.assigneeId ?? '未认领' }}</p>
              </div>
            </div>

            <div class="info-card glass glow-border full-width">
              <div class="info-icon">👥</div>
              <div class="info-body">
                <span class="info-label">候选 Worker</span>
                <div class="candidate-tags">
                  <span
                    v-for="id in (task.candidateWorkerIds || [])"
                    :key="id"
                    class="candidate-tag"
                  >
                    {{ id }}
                  </span>
                  <span v-if="!task.candidateWorkerIds?.length" class="empty-text">-</span>
                </div>
              </div>
            </div>

            <div class="info-card glass glow-border">
              <div class="info-icon">🕐</div>
              <div class="info-body">
                <span class="info-label">创建时间</span>
                <p class="info-val mono">{{ formatTime(task.createdAt) }}</p>
              </div>
            </div>

            <div class="info-card glass glow-border">
              <div class="info-icon">🔄</div>
              <div class="info-body">
                <span class="info-label">更新时间</span>
                <p class="info-val mono">{{ formatTime(task.updatedAt) }}</p>
              </div>
            </div>

            <div v-if="task.errorMessage" class="info-card glass glow-border full-width error-card">
              <div class="info-icon">⚠️</div>
              <div class="info-body">
                <span class="info-label error">错误信息</span>
                <p class="info-text error">{{ task.errorMessage }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty-state">
        <span class="empty-icon">🔍</span>
        <p>任务不存在或无权查看</p>
        <button class="back-link" @click="goBack">返回上一页</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useToast } from '../composables/useToast'
import { useStatus } from '../composables/useStatus'
import { getTaskDetail } from '../api'

const router = useRouter()
const route = useRoute()
const { isDark, isTableView, isCardView } = useTheme()
const toast = useToast()
const { getStatusLabel, getStatusColor, getStatusBg, getStatusIcon, formatTime } = useStatus()

const user = JSON.parse(localStorage.getItem('user'))
const task = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskDetail(route.params.id)
    task.value = res.data
  } catch (e) {
    task.value = null
    toast.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(user?.role === 'LEADER' ? '/leader' : '/worker')
}

onMounted(fetchDetail)
</script>

<style scoped>
.detail-page {
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

.back-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  font-size: 13px;
  font-weight: 600;
  font-family: inherit;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-btn:hover {
  background: var(--accent-light);
  color: var(--accent);
  border-color: var(--accent);
}

.header-divider {
  width: 1px;
  height: 24px;
  background: var(--border);
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
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

.badge-role {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  color: var(--text-tertiary);
}

/* ===== Content ===== */
.content {
  max-width: 900px;
  margin: 0 auto;
  padding: 28px;
}

/* ===== Loading ===== */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  gap: 16px;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.loading-state p {
  color: var(--text-tertiary);
  font-size: 14px;
}

/* ===== Crystal Detail ===== */
.detail-card {
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.detail-header {
  padding: 28px 32px;
  border-bottom: 1px solid var(--border);
}

.detail-title-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.detail-id {
  font-size: 14px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-tertiary);
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

.status-pill.lg {
  padding: 6px 16px;
  font-size: 14px;
}

.detail-title {
  font-size: 28px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0;
  line-height: 1.3;
}

.detail-body {
  padding: 28px 32px;
}

.detail-section {
  margin-bottom: 24px;
}

.section-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-tertiary);
  margin: 0 0 8px;
}

.section-label.error {
  color: #ef4444;
}

.section-content {
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
  margin: 0;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.item-label {
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-tertiary);
}

.item-value {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
}

.item-value.mono {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.user-chip {
  display: inline-block;
  padding: 2px 10px;
  background: var(--accent-light);
  color: var(--accent);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
}

.user-chip.worker {
  background: rgba(16, 185, 129, 0.1);
  color: #10b981;
}

.unassigned {
  color: var(--text-tertiary);
  font-style: italic;
}

.candidate-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.candidate-chip {
  padding: 4px 12px;
  background: var(--bg-tertiary);
  border: 1px solid var(--border);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-secondary);
}

.empty-text {
  color: var(--text-tertiary);
  font-size: 14px;
}

.error-section {
  padding: 16px 20px;
  background: rgba(239, 68, 68, 0.05);
  border: 1px solid rgba(239, 68, 68, 0.2);
  border-radius: var(--radius-md);
}

.error-content {
  font-size: 14px;
  line-height: 1.6;
  color: #ef4444;
  margin: 0;
  font-weight: 500;
}

/* ===== Obsidian Detail ===== */
.hero-card {
  position: relative;
  border-radius: var(--radius-xl);
  overflow: hidden;
  margin-bottom: 20px;
  padding: 40px 32px;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, var(--accent), #5c7cfa, #38d9a9);
  opacity: 0.1;
}

.hero-content {
  position: relative;
  z-index: 1;
}

.hero-id {
  display: inline-block;
  padding: 4px 12px;
  background: var(--accent-light);
  color: var(--accent);
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  margin-bottom: 16px;
}

.hero-title {
  font-size: 36px;
  font-weight: 800;
  color: var(--text-primary);
  margin: 0 0 16px;
  line-height: 1.2;
}

.hero-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  border: 2px solid;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-card {
  padding: 20px;
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
  display: flex;
  gap: 14px;
  transition: all 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.info-card.full-width {
  grid-column: 1 / -1;
}

.info-card.error-card {
  border-color: rgba(239, 68, 68, 0.3);
  background: rgba(239, 68, 68, 0.05);
}

.info-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.info-body {
  flex: 1;
  min-width: 0;
}

.info-label {
  display: block;
  font-size: 11px;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  color: var(--text-tertiary);
  margin-bottom: 4px;
}

.info-label.error {
  color: #ef4444;
}

.info-text {
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
  margin: 0;
}

.info-text.error {
  color: #ef4444;
  font-weight: 500;
}

.info-val {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0;
}

.info-val.mono {
  font-family: 'JetBrains Mono', monospace;
  font-size: 13px;
}

.candidate-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 4px;
}

.candidate-tag {
  padding: 3px 10px;
  background: var(--accent-light);
  color: var(--accent);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
}

/* ===== Empty State ===== */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
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
  margin: 0 0 16px;
}

.back-link {
  padding: 10px 20px;
  background: var(--accent);
  color: white;
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
}

.back-link:hover {
  background: var(--accent-hover);
  transform: translateY(-1px);
}

/* ===== Animations ===== */
.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width: 768px) {
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

  .header-divider {
    display: none;
  }

  .content {
    padding: 16px;
  }

  .detail-header {
    padding: 20px;
  }

  .detail-title {
    font-size: 22px;
  }

  .detail-body {
    padding: 20px;
  }

  .detail-grid {
    grid-template-columns: 1fr;
  }

  .hero-card {
    padding: 28px 20px;
  }

  .hero-title {
    font-size: 24px;
  }

  .info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
