import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', redirect: '/login' },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../pages/LoginPage.vue'),
  },
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

export default router
