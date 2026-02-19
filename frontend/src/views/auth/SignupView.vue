<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../../api/auth.js'

const router = useRouter()

const form = ref({ email: '', password: '', confirmPassword: '', nickname: '' })
const error = ref('')

async function handleSignup() {
  error.value = ''
  if (form.value.password !== form.value.confirmPassword) {
    error.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  try {
    await authApi.signup({
      email: form.value.email,
      password: form.value.password,
      nickname: form.value.nickname,
    })
    router.push('/login')
  } catch (e) {
    error.value = e.response?.data?.message || '회원가입에 실패했습니다.'
  }
}
</script>

<template>
  <div class="auth-container">
    <div class="auth-card">
      <h2>회원가입</h2>
      <form @submit.prevent="handleSignup">
        <div class="form-group">
          <label>이메일</label>
          <input v-model="form.email" type="email" placeholder="이메일을 입력하세요" required />
        </div>
        <div class="form-group">
          <label>닉네임</label>
          <input v-model="form.nickname" type="text" placeholder="닉네임을 입력하세요" required />
        </div>
        <div class="form-group">
          <label>비밀번호</label>
          <input v-model="form.password" type="password" placeholder="비밀번호를 입력하세요" required />
        </div>
        <div class="form-group">
          <label>비밀번호 확인</label>
          <input
            v-model="form.confirmPassword"
            type="password"
            placeholder="비밀번호를 다시 입력하세요"
            required
          />
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <button type="submit" class="btn-primary">회원가입</button>
      </form>
      <p class="link">
        이미 계정이 있으신가요? <router-link to="/login">로그인</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 56px);
}

.auth-card {
  width: 100%;
  max-width: 400px;
  padding: 32px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

h2 {
  margin-bottom: 24px;
  text-align: center;
  color: #333;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-size: 14px;
  color: #555;
}

.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.error {
  color: #e74c3c;
  font-size: 13px;
  margin-bottom: 12px;
}

.btn-primary {
  width: 100%;
  padding: 12px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 15px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #3a7bc8;
}

.link {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #888;
}

.link a {
  color: #4a90d9;
}
</style>
