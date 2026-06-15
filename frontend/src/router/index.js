import { createRouter, createWebHistory } from 'vue-router'
import LoginPage from '../pages/LoginPage.vue'

const routes = [
  { path: '/', redirect: '/login' },
  { path: '/login', name: 'Login', component: LoginPage },
  {
    path: '/leader',
    name: 'Leader',
    component: () => import('../pages/LeaderPage.vue'),
    meta: { requiresAuth: true, role: 'LEADER' },
  },
  {
    path: '/worker',
    name: 'Worker',
    component: () => import('../pages/WorkerPage.vue'),
    meta: { requiresAuth: true, role: 'WORKER' },
  },
  {
    path: '/task/:id',
    name: 'TaskDetail',
    component: () => import('../pages/TaskDetailPage.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  if (!to.meta.requiresAuth) return next()
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (!user) return next('/login')
  if (to.meta.role && to.meta.role !== user.role) {
    return next(user.role === 'LEADER' ? '/leader' : '/worker')
  }
  next()
})

/**
 * 登出：调用后端接口注销 Token，清除本地存储
 */
export async function doLogout() {
  try {
    const { logout } = await import('../api')
    await logout()
  } catch (e) {
    // 即使后端调用失败也清除本地状态
  }
  localStorage.removeItem('user')
  localStorage.removeItem('uid')
  router.push('/login')
}

export default router
