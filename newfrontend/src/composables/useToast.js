import { ref, readonly } from 'vue'

const toasts = ref([])
let toastId = 0

export function useToast() {
  const addToast = (message, type = 'info', duration = 3000) => {
    const id = ++toastId
    toasts.value.push({ id, message, type, visible: true })
    setTimeout(() => {
      removeToast(id)
    }, duration)
  }

  const removeToast = (id) => {
    const idx = toasts.value.findIndex(t => t.id === id)
    if (idx !== -1) {
      toasts.value[idx].visible = false
      setTimeout(() => {
        toasts.value = toasts.value.filter(t => t.id !== id)
      }, 300)
    }
  }

  const success = (msg) => addToast(msg, 'success')
  const error = (msg) => addToast(msg, 'error')
  const warning = (msg) => addToast(msg, 'warning')
  const info = (msg) => addToast(msg, 'info')

  return {
    toasts: readonly(toasts),
    success,
    error,
    warning,
    info,
    removeToast,
  }
}
