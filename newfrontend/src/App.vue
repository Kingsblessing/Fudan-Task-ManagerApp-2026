<template>
  <div :class="['app-root', { 'dark': isDark }]">
    <router-view v-slot="{ Component, route }">
      <transition name="page" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>

    <!-- Toast Notifications -->
    <ToastContainer />

    <!-- Floating Controls -->
    <div class="floating-controls">
      <!-- View Mode Switcher -->
      <div class="control-btn" @click="toggleViewMode" :title="`当前: ${viewModeLabel}视图，点击切换`">
        <transition name="icon-flip" mode="out-in">
          <svg v-if="isTableView" key="table" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="8" y1="6" x2="21" y2="6"/>
            <line x1="8" y1="12" x2="21" y2="12"/>
            <line x1="8" y1="18" x2="21" y2="18"/>
            <line x1="3" y1="6" x2="3.01" y2="6"/>
            <line x1="3" y1="12" x2="3.01" y2="12"/>
            <line x1="3" y1="18" x2="3.01" y2="18"/>
          </svg>
          <svg v-else key="card" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="3" y="3" width="7" height="7" rx="1"/>
            <rect x="14" y="3" width="7" height="7" rx="1"/>
            <rect x="3" y="14" width="7" height="7" rx="1"/>
            <rect x="14" y="14" width="7" height="7" rx="1"/>
          </svg>
        </transition>
        <span class="control-label">{{ viewModeLabel }}</span>
      </div>

      <!-- Theme Switcher -->
      <div class="control-btn" @click="cycleTheme" :title="`当前: ${themeLabel}，点击切换`">
        <transition name="icon-flip" mode="out-in">
          <svg v-if="currentTheme === 'auto'" key="auto" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <rect x="2" y="3" width="20" height="14" rx="2" ry="2"/>
            <line x1="8" y1="21" x2="16" y2="21"/>
            <line x1="12" y1="17" x2="12" y2="21"/>
          </svg>
          <svg v-else-if="isLight" key="light" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="5"/>
            <line x1="12" y1="1" x2="12" y2="3"/>
            <line x1="12" y1="21" x2="12" y2="23"/>
            <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"/>
            <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"/>
            <line x1="1" y1="12" x2="3" y2="12"/>
            <line x1="21" y1="12" x2="23" y2="12"/>
            <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"/>
            <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"/>
          </svg>
          <svg v-else key="dark" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/>
          </svg>
        </transition>
        <span class="control-label">{{ themeLabel }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useTheme } from './composables/useTheme'
import ToastContainer from './components/ToastContainer.vue'

const {
  currentTheme,
  isDark,
  isLight,
  isTableView,
  viewModeLabel,
  themeLabel,
  toggleViewMode,
} = useTheme()

const cycleTheme = () => {
  const themes = ['auto', 'light', 'dark']
  const currentIndex = themes.indexOf(currentTheme.value)
  const nextIndex = (currentIndex + 1) % themes.length
  const { setTheme } = useTheme()
  setTheme(themes[nextIndex])
}
</script>

<style scoped>
.app-root {
  min-height: 100vh;
  transition: all 0.4s ease;
}

/* Page transitions */
.page-enter-active,
.page-leave-active {
  transition: all 0.3s ease;
}

.page-enter-from {
  opacity: 0;
  transform: translateY(8px);
}

.page-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* Floating controls */
.floating-controls {
  position: fixed;
  bottom: 24px;
  right: 24px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.control-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 999px;
  cursor: pointer;
  box-shadow: var(--shadow-lg);
  color: var(--text-primary);
  transition: all 0.3s ease;
  user-select: none;
}

.control-btn:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-lg), 0 0 20px var(--accent-glow);
  border-color: var(--accent);
}

.control-label {
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

/* Icon flip animation */
.icon-flip-enter-active,
.icon-flip-leave-active {
  transition: all 0.3s ease;
}

.icon-flip-enter-from {
  transform: rotate(-90deg) scale(0.5);
  opacity: 0;
}

.icon-flip-leave-to {
  transform: rotate(90deg) scale(0.5);
  opacity: 0;
}

/* Responsive */
@media (max-width: 768px) {
  .floating-controls {
    bottom: 16px;
    right: 16px;
  }

  .control-btn {
    padding: 10px 12px;
  }

  .control-label {
    font-size: 11px;
  }
}
</style>
