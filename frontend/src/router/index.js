import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/auth/LoginView.vue'),
    meta: { guest: true },
  },
  {
    path: '/signup',
    name: 'Signup',
    component: () => import('../views/auth/SignupView.vue'),
    meta: { guest: true },
  },
  {
    path: '/',
    name: 'Dashboard',
    component: () => import('../views/dashboard/DashboardView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/transactions',
    name: 'Transactions',
    component: () => import('../views/transaction/TransactionListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/categories',
    name: 'Categories',
    component: () => import('../views/category/CategoryListView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/export',
    name: 'Export',
    component: () => import('../views/transaction/ExportView.vue'),
    meta: { requiresAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 네비게이션 가드 - 인증 체크
router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return '/login'
  }
  if (to.meta.guest && authStore.isLoggedIn) {
    return '/'
  }
})

export default router
