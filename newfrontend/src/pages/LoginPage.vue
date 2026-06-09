<template>
  <div :class="['login-page', { 'dark-mode': isDark }]">
    <!-- Background effects -->
    <div class="bg-effects">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="grid-pattern"></div>
    </div>

    <!-- Main content -->
    <div class="login-wrapper animate-fade-in">
      <!-- Logo / Brand -->
      <div class="brand-section">
        <div class="logo-mark">
          <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
            <defs>
              <linearGradient id="logoGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                <stop offset="0%" :style="{ stopColor: isDark ? '#845ef7' : '#5c7cfa' }" />
                <stop offset="100%" :style="{ stopColor: isDark ? '#5c7cfa' : '#3b82f6' }" />
              </linearGradient>
            </defs>
            <rect width="48" height="48" rx="14" fill="url(#logoGrad)" />
            <path d="M15 24l6 6 12-12" stroke="white" stroke-width="3" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h1 class="brand-title">Task Manager</h1>
        <p class="brand-subtitle">{{ isDark ? '深色模式' : '浅色模式' }}</p>
      </div>

      <!-- Login Card -->
      <div class="login-card glass">
        <div class="card-inner">
          <h2 class="card-title">欢迎回来</h2>
          <p class="card-desc">请登录以继续使用任务管理系统</p>

          <form @submit.prevent="handleLogin" class="login-form">
            <!-- User ID Input -->
            <div class="form-group">
              <label class="form-label">用户 ID</label>
              <div class="input-wrapper">
                <span class="input-icon">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                    <circle cx="12" cy="7" r="4"/>
                  </svg>
                </span>
                <input
                  v-model.number="form.userId"
                  type="number"
                  placeholder="输入用户 ID (如 1001)"
                  class="form-input"
                  min="1001"
                  max="9999"
                  required
                />
              </div>
            </div>

            <!-- Role Selection -->
            <div class="form-group">
              <label class="form-label">选择角色</label>
              <div class="role-selector">
                <button
                  type="button"
                  :class="['role-btn', { active: form.role === 'LEADER' }]"
                  @click="form.role = 'LEADER'"
                >
                  <span class="role-icon">👑</span>
                  <span class="role-text">Leader</span>
                  <span class="role-desc">管理员</span>
                </button>
                <button
                  type="button"
                  :class="['role-btn', { active: form.role === 'WORKER' }]"
                  @click="form.role = 'WORKER'"
                >
                  <span class="role-icon">⚡</span>
                  <span class="role-text">Worker</span>
                  <span class="role-desc">执行者</span>
                </button>
              </div>
            </div>

            <!-- Submit -->
            <button type="submit" class="submit-btn" :disabled="loading">
              <span v-if="loading" class="spinner"></span>
              <span v-else>登 录</span>
            </button>
          </form>

          <!-- Divider -->
          <div class="divider">
            <span>测试账号</span>
          </div>

          <!-- Test Accounts -->
          <div class="test-accounts">
            <div
              v-for="account in testAccounts"
              :key="account.id"
              :class="['account-chip', account.role.toLowerCase()]"
              @click="quickLogin(account)"
            >
              <span class="chip-id">{{ account.id }}</span>
              <span class="chip-role">{{ account.role }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Theme hint -->
      <p class="theme-hint">
        点击右下角按钮切换主题和视图模式
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useTheme } from '../composables/useTheme'
import { useToast } from '../composables/useToast'
import { login } from '../api'

const router = useRouter()
const { isDark } = useTheme()
const toast = useToast()
const loading = ref(false)
const form = reactive({ userId: null, role: 'LEADER' })

const testAccounts = [
  { id: 1001, role: 'LEADER' },
  { id: 1002, role: 'LEADER' },
  { id: 2001, role: 'WORKER' },
  { id: 2002, role: 'WORKER' },
  { id: 2003, role: 'WORKER' },
  { id: 2004, role: 'WORKER' },
  { id: 2005, role: 'WORKER' },
]

const quickLogin = (account) => {
  form.userId = account.id
  form.role = account.role
}

const handleLogin = async () => {
  if (!form.userId) {
    toast.warning('请输入用户 ID')
    return
  }
  loading.value = true
  try {
    await login({ userId: form.userId, role: form.role })
    localStorage.setItem('user', JSON.stringify({ userId: form.userId, role: form.role }))
    toast.success('登录成功')
    setTimeout(() => {
      router.push(form.role === 'LEADER' ? '/leader' : '/worker')
    }, 500)
  } catch (e) {
    toast.error(e.message || '登录失败，请检查用户 ID 和角色')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  position: relative;
  overflow: hidden;
  background: var(--bg-primary);
}

/* ===== Background Effects ===== */
.bg-effects {
  position: fixed;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 8s ease-in-out infinite;
}

.orb-1 {
  width: 400px;
  height: 400px;
  background: var(--accent);
  top: -100px;
  right: -100px;
  animation-delay: 0s;
}

.orb-2 {
  width: 300px;
  height: 300px;
  background: #38d9a9;
  bottom: -50px;
  left: -50px;
  animation-delay: -3s;
}

.orb-3 {
  width: 200px;
  height: 200px;
  background: #f59e0b;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation-delay: -5s;
  opacity: 0.2;
}

.grid-pattern {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(92, 124, 250, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(92, 124, 250, 0.03) 1px, transparent 1px);
  background-size: 60px 60px;
}

.dark-mode .grid-pattern {
  background-image:
    linear-gradient(rgba(132, 94, 247, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(132, 94, 247, 0.06) 1px, transparent 1px);
}

/* ===== Login Wrapper ===== */
.login-wrapper {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* ===== Brand Section ===== */
.brand-section {
  text-align: center;
  margin-bottom: 32px;
}

.logo-mark {
  margin-bottom: 16px;
  display: inline-block;
  animation: float 4s ease-in-out infinite;
}

.brand-title {
  font-size: 32px;
  font-weight: 800;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, var(--accent), #38d9a9);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 4px;
}

.brand-subtitle {
  font-size: 13px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 2px;
  color: var(--text-tertiary);
}

/* ===== Login Card ===== */
.login-card {
  width: 100%;
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.card-inner {
  padding: 40px 36px;
}

.card-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
  color: var(--text-primary);
}

.card-desc {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 28px;
}

/* ===== Form ===== */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 14px;
  color: var(--text-tertiary);
  pointer-events: none;
  display: flex;
  align-items: center;
}

.form-input {
  width: 100%;
  padding: 14px 14px 14px 44px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border);
  border-radius: var(--radius-md);
  font-size: 15px;
  font-family: inherit;
  color: var(--text-primary);
  transition: all 0.2s ease;
  outline: none;
}

.form-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 4px var(--accent-light);
}

.form-input::placeholder {
  color: var(--text-tertiary);
}

/* ===== Role Selector ===== */
.role-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.role-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 16px 12px;
  background: var(--bg-tertiary);
  border: 2px solid var(--border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: inherit;
  color: var(--text-primary);
}

.role-btn:hover {
  border-color: var(--border-hover);
  transform: translateY(-2px);
}

.role-btn.active {
  border-color: var(--accent);
  background: var(--accent-light);
  box-shadow: 0 0 0 4px var(--accent-light);
}

.role-icon {
  font-size: 24px;
  margin-bottom: 2px;
}

.role-text {
  font-size: 15px;
  font-weight: 700;
}

.role-desc {
  font-size: 11px;
  color: var(--text-tertiary);
  font-weight: 500;
}

/* ===== Submit Button ===== */
.submit-btn {
  width: 100%;
  padding: 16px;
  background: linear-gradient(135deg, var(--accent), var(--accent-hover));
  color: white;
  border: none;
  border-radius: var(--radius-md);
  font-size: 16px;
  font-weight: 700;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px var(--accent-glow);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* ===== Divider ===== */
.divider {
  display: flex;
  align-items: center;
  gap: 16px;
  margin: 28px 0 20px;
  color: var(--text-tertiary);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.divider::before,
.divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: var(--border);
}

/* ===== Test Accounts ===== */
.test-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
}

.account-chip {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 999px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid var(--border);
  background: var(--bg-tertiary);
}

.account-chip:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.account-chip.leader {
  border-color: rgba(245, 158, 11, 0.3);
}

.account-chip.leader:hover {
  background: rgba(245, 158, 11, 0.1);
  border-color: #f59e0b;
}

.account-chip.worker {
  border-color: rgba(16, 185, 129, 0.3);
}

.account-chip.worker:hover {
  background: rgba(16, 185, 129, 0.1);
  border-color: #10b981;
}

.chip-id {
  font-weight: 700;
  font-family: 'JetBrains Mono', monospace;
  color: var(--text-primary);
}

.chip-role {
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  color: var(--text-tertiary);
}

/* ===== Theme Hint ===== */
.theme-hint {
  margin-top: 24px;
  font-size: 12px;
  color: var(--text-tertiary);
  text-align: center;
}

.theme-hint strong {
  color: var(--accent);
}

/* ===== Animations ===== */
@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-20px); }
}

.animate-fade-in {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

/* ===== Responsive ===== */
@media (max-width: 480px) {
  .card-inner {
    padding: 28px 24px;
  }

  .brand-title {
    font-size: 26px;
  }

  .test-accounts {
    gap: 6px;
  }

  .account-chip {
    padding: 6px 10px;
    font-size: 12px;
  }
}
</style>
