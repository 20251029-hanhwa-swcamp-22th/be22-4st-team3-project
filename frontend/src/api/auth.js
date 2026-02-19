import api from './index.js'

export const authApi = {
  signup(data) {
    return api.post('/auth/signup', data)
  },
  login(data) {
    return api.post('/auth/login', data)
  },
  logout() {
    return api.post('/auth/logout')
  },
}
