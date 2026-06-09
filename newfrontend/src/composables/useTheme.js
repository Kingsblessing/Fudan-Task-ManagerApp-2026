import { ref, watch, onMounted, computed } from 'vue'

const THEME_KEY = 'app-theme'
const VIEW_KEY = 'app-view-mode'

// Theme: 'light' | 'dark'
const currentTheme = ref(localStorage.getItem(THEME_KEY) || 'auto')

// View mode: 'table' | 'card'
const viewMode = ref(localStorage.getItem(VIEW_KEY) || 'table')

// System theme detection
const systemTheme = ref('light')

export function useTheme() {
  // Detect system theme
  const detectSystemTheme = () => {
    if (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches) {
      systemTheme.value = 'dark'
    } else {
      systemTheme.value = 'light'
    }
  }

  // Computed: actual theme based on currentTheme setting
  const resolvedTheme = computed(() => {
    if (currentTheme.value === 'auto') {
      return systemTheme.value
    }
    return currentTheme.value
  })

  const isDark = computed(() => resolvedTheme.value === 'dark')
  const isLight = computed(() => resolvedTheme.value === 'light')

  // View mode checks
  const isTableView = computed(() => viewMode.value === 'table')
  const isCardView = computed(() => viewMode.value === 'card')

  // Theme setters
  const setTheme = (theme) => {
    currentTheme.value = theme
    localStorage.setItem(THEME_KEY, theme)
    applyTheme()
  }

  const toggleTheme = () => {
    if (currentTheme.value === 'auto') {
      setTheme(systemTheme.value === 'dark' ? 'light' : 'dark')
    } else {
      setTheme(currentTheme.value === 'light' ? 'dark' : 'light')
    }
  }

  const setAutoTheme = () => {
    setTheme('auto')
  }

  // View mode setters
  const setViewMode = (mode) => {
    viewMode.value = mode
    localStorage.setItem(VIEW_KEY, mode)
  }

  const toggleViewMode = () => {
    setViewMode(viewMode.value === 'table' ? 'card' : 'table')
  }

  // Apply theme to DOM
  const applyTheme = () => {
    const html = document.documentElement
    if (isDark.value) {
      html.setAttribute('data-theme', 'dark')
      html.classList.add('dark')
    } else {
      html.removeAttribute('data-theme')
      html.classList.remove('dark')
    }
  }

  // Get theme display label
  const themeLabel = computed(() => {
    if (currentTheme.value === 'auto') {
      return `自动` // (${systemTheme.value === 'dark' ? '深色' : '浅色'})
    }
    return currentTheme.value === 'dark' ? '深色' : '浅色'
  })

  // Get view mode display label
  const viewModeLabel = computed(() => {
    return viewMode.value === 'table' ? '列表' : '卡片'
  })

  // Initialize
  onMounted(() => {
    detectSystemTheme()
    applyTheme()

    // Listen for system theme changes
    if (window.matchMedia) {
      window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
        systemTheme.value = e.matches ? 'dark' : 'light'
        if (currentTheme.value === 'auto') {
          applyTheme()
        }
      })
    }
  })

  // Watch for theme changes
  watch(resolvedTheme, () => {
    applyTheme()
  })

  return {
    // Theme
    currentTheme,
    resolvedTheme,
    isDark,
    isLight,
    systemTheme,
    themeLabel,
    setTheme,
    toggleTheme,
    setAutoTheme,

    // View mode
    viewMode,
    isTableView,
    isCardView,
    viewModeLabel,
    setViewMode,
    toggleViewMode,
  }
}
