import axios from 'axios'
import { useAuthStore } from '../stores/auth.js'
import router from '../router/index.js'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 요청 인터셉터 - JWT 토큰 자동 첨부
api.interceptors.request.use((config) => {
  const authStore = useAuthStore()
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`
  }
  return config
})

// 응답 인터셉터 - 401 시 갱신 시도 및 로그인 페이지로 이동
api.interceptors.response.use(
  response => response.config.responseType === 'blob' ? response.data : response.data.data,
    async error => {
        const authStore = useAuthStore()
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                const data = await api.post('/auth/refresh');
                const newToken = data.accessToken;
                authStore.setAuth(newToken, authStore.user);
                originalRequest.headers.Authorization = `Bearer ${newToken}`;
                return api(originalRequest);
            } catch {
                authStore.logout();
                router.push('/login');
            }
        }
        return Promise.reject(error);
    }
)

export default api
