<script setup>
import { useAuthStore } from '../../stores/auth.js'
import { useRouter } from 'vue-router'
import { ref } from 'vue'
import { authApi } from '../../api/auth.js'

const authStore = useAuthStore()
const router = useRouter()
const logoutLoading = ref(false)

async function handleLogout() {
  if (logoutLoading.value) return
  logoutLoading.value = true
  try {
    await authApi.logout()
  } catch {
    // Force client logout even if server token cleanup fails.
  } finally {
    authStore.logout()
    logoutLoading.value = false
    router.push('/login')
  }
}
</script>

<template>
  <header class="app-header">
    <div class="header-left">
      <router-link to="/" class="logo">가계부</router-link>
      <nav v-if="authStore.isLoggedIn">
        <router-link to="/">대시보드</router-link>
        <router-link to="/transactions">거래내역</router-link>
        <router-link to="/categories">카테고리</router-link>
        <router-link to="/export">내보내기</router-link>
      </nav>
    </div>
    <div class="header-right" v-if="authStore.isLoggedIn">
      <span class="nickname">{{ authStore.user?.nickname }}</span>
      <button @click="handleLogout" :disabled="logoutLoading" class="btn-logout">
        {{ logoutLoading ? '로그아웃 중...' : '로그아웃' }}
      </button>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.logo {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  text-decoration: none;
}

nav {
  display: flex;
  gap: 16px;
}

nav a {
  text-decoration: none;
  color: #666;
  font-size: 14px;
  padding: 4px 0;
  border-bottom: 2px solid transparent;
}

nav a.router-link-active {
  color: #4a90d9;
  border-bottom-color: #4a90d9;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nickname {
  font-size: 14px;
  color: #666;
}

.btn-logout {
  padding: 6px 12px;
  font-size: 13px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.btn-logout:hover {
  background: #f5f5f5;
}

.btn-logout:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
