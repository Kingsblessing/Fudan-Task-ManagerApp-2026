<template>
  <teleport to="body">
    <div class="toast-container">
      <transition-group name="toast">
        <div
          v-for="toast in toasts"
          :key="toast.id"
          :class="['toast-item', `toast-${toast.type}`]"
          @click="removeToast(toast.id)"
        >
          <span class="toast-icon">{{ iconMap[toast.type] }}</span>
          <span class="toast-msg">{{ toast.message }}</span>
        </div>
      </transition-group>
    </div>
  </teleport>
</template>

<script setup>
import { useToast } from '../composables/useToast'

const { toasts, removeToast } = useToast()

const iconMap = {
  success: '✓',
  error: '✕',
  warning: '!',
  info: 'i',
}
</script>

<style scoped>
.toast-container {
  position: fixed;
  top: 24px;
  right: 24px;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
}

.toast-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  pointer-events: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(12px);
  min-width: 240px;
  animation: slideIn 0.3s ease-out;
}

.toast-success {
  background: rgba(16, 185, 129, 0.95);
  color: white;
}

.toast-error {
  background: rgba(239, 68, 68, 0.95);
  color: white;
}

.toast-warning {
  background: rgba(245, 158, 11, 0.95);
  color: white;
}

.toast-info {
  background: rgba(59, 130, 246, 0.95);
  color: white;
}

.toast-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.25);
  flex-shrink: 0;
}

.toast-msg {
  flex: 1;
}

/* Transitions */
.toast-enter-active {
  transition: all 0.3s ease-out;
}

.toast-leave-active {
  transition: all 0.2s ease-in;
}

.toast-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.toast-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

@keyframes slideIn {
  from {
    transform: translateX(100%);
    opacity: 0;
  }
  to {
    transform: translateX(0);
    opacity: 1;
  }
}
</style>
